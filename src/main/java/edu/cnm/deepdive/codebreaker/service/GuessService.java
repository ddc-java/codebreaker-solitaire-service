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
public class GuessService {

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

  /**
   * Validates and matches the specified {@link Guess} against the related {@link Game}, and adds
   * the former to the latter's collection of guesses. {@link Guess#getText() guess.getText()}{@link
   * String#length() .length()} must be equal to {@link Game#getLength() game.getLength()}, and
   * {@link Guess#getText() guess.getText()} must not include any characters that are not present in
   * {@link Game#getPool() game.getPool()}. If these conditions are met, then {@link
   * Guess#getText()} and {@link Game#getText()} are compared, to compute the number of exact and
   * near-matches. Finally, {@code guess} is saved to the collection, as one of {@code game}'s
   * guesses.
   *
   * @param game  Game with secret code being guessed.
   * @param guess Submitted attempt to guess the secret code.
   * @return Validated, summarized (in the numbers of matches), and saved {@code Guess}.
   */
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

  /**
   * Retrieves an {@link Optional Optional&lt;Guess&gt;}, specified by {@code externalKey}, from the
   * collection. If there is no instance with the specified {@code externalKey} in the collection,
   * the {@link Optional} returned is empty.
   *
   * @param externalKey Unique identifier of {@link Guess} instance.
   * @return {@link Optional Optional&lt;Guess&gt;} containing {@link Guess} referenced by {@code
   * externalKey} (if it exists).
   */
  public Optional<Guess> get(@NonNull UUID externalKey) {
    try {
      return guessRepository.findByExternalKey(externalKey);
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
