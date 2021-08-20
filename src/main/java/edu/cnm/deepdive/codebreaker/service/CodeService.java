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

import edu.cnm.deepdive.codebreaker.controller.CodebreakerExceptionHandler.InvalidPropertyException;
import edu.cnm.deepdive.codebreaker.model.dao.CodeRepository;
import edu.cnm.deepdive.codebreaker.model.entity.Code;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

/**
 * Implements high-level operations on new and existing {@link Code} instances. These operations
 * include validating the character pools for new codes, by removing duplicated characters and
 * checking for whitespace, control, and undefined (i.e. not present in the Unicode Character
 * Database) characters; generating the random text for new codes; retrieving a single code using
 * its "stringified" id; defining and applying query filters to select all codes, solved codes only,
 * and unsolved codes only; deleting a single code; and deleting all codes.
 */
@Service
public class CodeService {

  private static final String POOL_PROPERTY = "pool";
  private static final String INVALID_CHARACTER_MESSAGE =
      "must not contain whitespace, control, or undefined characters";
  private static final String STATUS_PROPERTY = "status";
  private static final String INVALID_STATUS_MESSAGE =
      String.format("must be one of %s (case-insensitive).", Arrays.toString(Status.values()));

  private final CodeRepository codeRepository;
  private final UUIDStringifier stringifier;
  private final Random rng;

  /**
   * Initalizes the service with a {@link CodeRepository}, {@link UUIDStringifier}, and {@link
   * Random} (i.e. a source of randomness).
   *
   * @param codeRepository Persistence operations provider.
   * @param stringifier    Converter between {@link UUID} (primary key) instances and {@link String}
   *                       representations.
   * @param rng            Source of randomness (for generating random codes).
   */
  @Autowired
  public CodeService(CodeRepository codeRepository, UUIDStringifier stringifier, Random rng) {
    this.codeRepository = codeRepository;
    this.stringifier = stringifier;
    this.rng = rng;
  }

  /**
   * Validates, completes, and adds the partially-specified {@link Code} instance to the collection.
   * Minimally, {@code code} must contain a character pool and code length; if the text of the code
   * is not included (which is always the case for {@link Code} instances deserialized from JSON),
   * random text is automatically generated.
   *
   * @param code Partial {@link Code}.
   * @return Completed and persisted {@link Code} instance.
   * @throws InvalidPropertyException If {@code code} contains any invalid characters (whitespace,
   *                                  control characters, or characters not included in the UCD).
   */
  public Code add(@NonNull Code code) throws InvalidPropertyException {
    int[] pool = code
        .getPool()
        .codePoints()
        .distinct()
        .toArray();
    if (
        IntStream
            .of(pool)
            .anyMatch(CodeService::isInvalidCodePoint)
    ) {
      throw new InvalidPropertyException(POOL_PROPERTY, INVALID_CHARACTER_MESSAGE);
    }
    code.setPool(new String(pool, 0, pool.length));
    if (code.getText() == null) {
      int[] secret = IntStream
          .generate(() -> pool[rng.nextInt(pool.length)])
          .limit(code.getLength())
          .toArray();
      String text = new String(secret, 0, secret.length);
      code.setText(text);
    }
    return codeRepository.save(code);
  }

  /**
   * Retrieves an {@link Optional Optional&lt;Code&gt;}, specified by {@code key}, from the
   * collection. If there is no instance with the specified {@code key} in the collection, the
   * {@link Optional} returned is empty.
   *
   * @param key Unique identifier of {@link Code} instance.
   * @return {@link Optional Optional&lt;Code&gt;} containing {@link Code} referenced by {@code key}
   * (if it exists).
   */
  public Optional<Code> get(@NonNull String key) {
    try {
      UUID id = stringifier.fromString(key);
      return codeRepository.findById(id);
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  /**
   * Removes the specified {@link Code} instance from the collection.
   *
   * @param code {@link Code} to be removed.
   */
  public void remove(@NonNull Code code) {
    codeRepository.delete(code);
  }

  /**
   * Returns a subset of {@link Code} instances from the collection, filtered by {@code
   * statusString}. If {@code statusString}, when converted to uppercase, matches one of the
   * enumerated {@link Status#values()} values} of {@link Status}, the corresponding subset is
   * returned; otherwise {@link IllegalArgumentException} is thrown.
   *
   * @param statusString Filter keyword, matching (when converted to uppercase) one of {@link
   *                     Status#values()}.
   * @return Subset of codes collection, filtered by {@code statusString}.
   * @throws InvalidPropertyException If {@code statusString} does not match one of {@link
   *                                  Status#values()}.
   */
  public Iterable<Code> list(@NonNull String statusString) throws InvalidPropertyException {
    try {
      Status status = Status.valueOf(statusString.toUpperCase());
      Iterable<Code> selection;
      switch (status) {
        case ALL:
          selection = codeRepository.getAllByOrderByCreatedDesc();
          break;
        case UNSOLVED:
          selection = codeRepository.getAllUnsolvedOrderByCreatedDesc();
          break;
        case SOLVED:
          selection = codeRepository.getAllSolvedOrderByCreatedDesc();
          break;
        default:
          selection = List.of();
          break;
      }
      return selection;
    } catch (IllegalArgumentException e) {
      throw new InvalidPropertyException(STATUS_PROPERTY, INVALID_STATUS_MESSAGE);
    }
  }

  /**
   * Removes all codes from the collection.
   */
  public void clear() {
    codeRepository.deleteAll();
  }

  private static boolean isInvalidCodePoint(int codePoint) {
    return !Character.isDefined(codePoint)
        || Character.isWhitespace(codePoint)
        || Character.isISOControl(codePoint);
  }

  /**
   * Allowed filter values for retrieving {@link Code} subsets from the collection.
   */
  public enum Status {

    /**
     * All codes, whether solved or unsolved.
     */
    ALL,
    /**
     * Unsolved codes only.
     */
    UNSOLVED,
    /**
     * Solved codes only.
     */
    SOLVED

  }

}
