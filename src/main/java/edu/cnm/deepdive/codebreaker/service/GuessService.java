/*
 *  Copyright 2021 CNM Ingenuity, Inc.
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
import edu.cnm.deepdive.codebreaker.model.entity.Code;
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
 * (for length and included characters) against the related {@link Code}; computing the number of
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
  private final UUIDStringifier stringifier;

  /**
   * Initializes this service with a {@link GuessRepository} and {@link UUIDStringifier}.
   *
   * @param guessRepository Persistence operations provider.
   * @param stringifier     Converter between {@link UUID} (primary key) instances and {@link
   *                        String} representations.
   */
  @Autowired
  public GuessService(GuessRepository guessRepository, UUIDStringifier stringifier) {
    this.guessRepository = guessRepository;
    this.stringifier = stringifier;
  }

  /**
   * Validates and matches the specified {@link Guess} against the related {@link Code}, and adds
   * the former to the latter's collection of guesses. {@link Guess#getText() code.getText()}{@link
   * String#length() .length()} must be equal to {@link Code#getLength() code.getLength()}, and
   * {@link Guess#getText() guess.getText()} must not include any characters that are not present in
   * {@link Code#getPool() code.getPool()}. If these conditions are met, then {@link
   * Guess#getText()} and {@link Code#getText()} are compared, to compute the number of exact and
   * near-matches. Finally, {@code guess} is saved to the collection, as one of {@code code}'s
   * guesses.
   *
   * @param code  Secret being guessed.
   * @param guess Submitted attempt to guess the secret code.
   * @return Validated, summarized (in the numbers of matches), and saved {@code Guess}.
   */
  public Guess add(@NonNull Code code, @NonNull Guess guess) throws InvalidPropertyException {
    validate(code, guess);
    int numCorrect = 0;
    int[] codeCodePoints = codePoints(code.getText());
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
    guess.setCode(code);
    return guessRepository.save(guess);
  }

  /**
   * Retrieves an {@link Optional Optional&lt;Guess&gt;}, specified by {@code key}, from the
   * collection. If there is no instance with the specified {@code key} in the collection, the
   * {@link Optional} returned is empty.
   *
   * @param key Unique identifier of {@link Guess} instance.
   * @return {@link Optional Optional&lt;Guess&gt;} containing {@link Guess} referenced by {@code
   * key} (if it exists).
   */
  public Optional<Guess> get(@NonNull String key) {
    try {
      UUID id = stringifier.fromString(key);
      return guessRepository.findById(id);
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  private void validate(Code code, Guess guess) throws InvalidPropertyException {
    if (code.isSolved()) {
      throw new AlreadySolvedException();
    }
    String guessText = guess.getText();
    String poolText = code.getPool();
    if (guessText.matches(String.format(INVALID_CHARACTERS, code.getPool()))) {
      throw new InvalidPropertyException(
          TEXT_PROPERTY, String.format(INVALID_CHARACTER_FORMAT, poolText));
    }
    if (codePoints(guess.getText()).length != code.getLength()) {
      throw new InvalidPropertyException(
          TEXT_PROPERTY, String.format(INVALID_LENGTH_FORMAT, code.getLength()));
    }
  }

  private int[] codePoints(String source) {
    return source
        .codePoints()
        .toArray();
  }

}
