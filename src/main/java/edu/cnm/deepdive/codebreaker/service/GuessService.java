/*
 *  Copyright 2022 CNM Ingenuity, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package edu.cnm.deepdive.codebreaker.service;

import edu.cnm.deepdive.codebreaker.controller.CodebreakerExceptionHandler.AlreadySolvedException;
import edu.cnm.deepdive.codebreaker.controller.CodebreakerExceptionHandler.InvalidPropertyException;
import edu.cnm.deepdive.codebreaker.model.dao.GuessRepository;
import edu.cnm.deepdive.codebreaker.model.entity.Game;
import edu.cnm.deepdive.codebreaker.model.entity.Guess;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

/**
 * Implements high-level operations on {@link Guess} instances. These include validating a new guess
 * (for length and included characters) against the related {@link Game}; computing the number of
 * exact matches and "near matches" between the text of a guess and the text of the code; saving a
 * validated and matched guess to the collection; and retrieving an individual guess using its
 * unique key.
 */
@Service
public class GuessService implements AbstractGuessService {

  private static final String INVALID_CHARACTERS = "^.*[^%s].*$";
  private static final String TEXT_PROPERTY = "text";
  private static final String INVALID_CHARACTER_FORMAT =
      "must contain no characters other than \"%s\"";
  private static final String INVALID_LENGTH_FORMAT =
      "must have a length exactly equal to the code length (%d characters)";

  private final GuessRepository guessRepository;

  /**
   * Initializes this service with a {@link GuessRepository}.
   *
   * @param guessRepository Persistence operations provider.
   */
  @Autowired
  public GuessService(GuessRepository guessRepository) {
    this.guessRepository = guessRepository;
  }

  @Override
  public Guess add(@NonNull Game game, @NonNull Guess guess) throws InvalidPropertyException {
    validate(game, guess);
    int numCorrect = 0;
    int[] codeCodePoints = codePoints(game.getText());
    int[] guessCodePoints = codePoints(guess.getText());
    Map<Integer, Integer> codeOccurrences = new HashMap<>();
    Map<Integer, Integer> guessOccurrences = new HashMap<>();
    for (int i = 0; i < guessCodePoints.length; i++) {
      int guessCodePoint = guessCodePoints[i];
      int codeCodePoint = codeCodePoints[i];
      if (guessCodePoint == codeCodePoint) {
        numCorrect++;
      } else {
        guessOccurrences.put(guessCodePoint, 1 + guessOccurrences.getOrDefault(guessCodePoint, 0));
        codeOccurrences.put(codeCodePoint, 1 + codeOccurrences.getOrDefault(codeCodePoint, 0));
      }
    }
    int numClose = guessOccurrences
        .entrySet()
        .stream()
        .mapToInt((entry) ->
            Math.min(entry.getValue(), codeOccurrences.getOrDefault(entry.getKey(), 0)))
        .sum();
    guess.setExactMatches(numCorrect);
    guess.setNearMatches(numClose);
    guess.setGame(game);
    return guessRepository.save(guess);
  }

  @Override
  public Optional<Guess> get(@NonNull Game game, @NonNull UUID externalKey) {
    try {
      return guessRepository.findByGameAndExternalKey(game, externalKey);
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  private void validate(Game game, Guess guess) throws InvalidPropertyException {
    if (game.isSolved()) {
      throw new AlreadySolvedException();
    }
    String guessText = guess.getText();
    String poolText = game.getPool();
    if (guessText.matches(String.format(INVALID_CHARACTERS, game.getPool()))) {
      throw new InvalidPropertyException(
          TEXT_PROPERTY, String.format(INVALID_CHARACTER_FORMAT, poolText));
    }
    if (codePoints(guess.getText()).length != game.getLength()) {
      throw new InvalidPropertyException(
          TEXT_PROPERTY, String.format(INVALID_LENGTH_FORMAT, game.getLength()));
    }
  }

  private int[] codePoints(String source) {
    return source
        .codePoints()
        .toArray();
  }

}
