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

import edu.cnm.deepdive.codebreaker.controller.CodebreakerExceptionHandler.InvalidPropertyException;
import edu.cnm.deepdive.codebreaker.model.dao.GameRepository;
import edu.cnm.deepdive.codebreaker.model.entity.Game;
import java.util.Arrays;
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
 * Implements high-level operations on new and existing {@link Game} instances. These operations
 * include validating the character pools for new codes, by removing duplicated characters and
 * checking for whitespace, control, and undefined (i.e. not present in the Unicode Character
 * Database) characters; generating the random text for new codes; retrieving a single game using
 * its external key; defining and applying query filters to select all codes, solved codes only,
 * and unsolved codes only; deleting a single game; and deleting all codes.
 */
@Service
public class GameService {

  private static final String POOL_PROPERTY = "pool";
  private static final String INVALID_CHARACTER_MESSAGE =
      "must not contain whitespace, control, or undefined characters";
  private static final String STATUS_PROPERTY = "status";
  private static final String INVALID_STATUS_MESSAGE =
      String.format("must be one of %s (case-insensitive).", Arrays.toString(Status.values()));

  private final GameRepository gameRepository;
  private final Random rng;

  /**
   * Initializes the service with a {@link GameRepository} and {@link Random} (i.e. a source of
   * randomness).
   *
   * @param gameRepository Persistence operations provider.
   * @param rng            Source of randomness (for generating random codes).
   */
  @Autowired
  public GameService(GameRepository gameRepository, Random rng) {
    this.gameRepository = gameRepository;
    this.rng = rng;
  }

  /**
   * Validates, completes, and adds the partially-specified {@link Game} instance to the collection.
   * Minimally, {@code game} must contain a character pool and game length; if the text of the game
   * is not included (which is always the case for {@link Game} instances deserialized from JSON),
   * random text is automatically generated.
   *
   * @param game Partial {@link Game}.
   * @return Completed and persisted {@link Game} instance.
   * @throws InvalidPropertyException If {@code game} contains any invalid characters (whitespace,
   *                                  control characters, or characters not included in the UCD).
   */
  public Game add(@NonNull Game game) throws InvalidPropertyException {
    int[] pool = game
        .getPool()
        .codePoints()
        .distinct()
        .toArray();
    if (
        IntStream
            .of(pool)
            .anyMatch(GameService::isInvalidCodePoint)
    ) {
      throw new InvalidPropertyException(POOL_PROPERTY, INVALID_CHARACTER_MESSAGE);
    }
    game.setPool(new String(pool, 0, pool.length));
    if (game.getText() == null) {
      int[] secret = IntStream
          .generate(() -> pool[rng.nextInt(pool.length)])
          .limit(game.getLength())
          .toArray();
      String text = new String(secret, 0, secret.length);
      game.setText(text);
    }
    return gameRepository.save(game);
  }

  /**
   * Retrieves an {@link Optional Optional&lt;Game&gt;}, specified by {@code externalKey}, from the
   * collection. If there is no instance with the specified {@code externalKey} in the collection,
   * the {@link Optional} returned is empty.
   *
   * @param externalKey Unique identifier of {@link Game} instance.
   * @return {@link Optional Optional&lt;Game&gt;} containing {@link Game} referenced by {@code
   * externalKey} (if it exists).
   */
  public Optional<Game> get(@NonNull UUID externalKey) {
    try {
      return gameRepository.findByExternalKey(externalKey);
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  /**
   * Removes the specified {@link Game} instance from the collection.
   *
   * @param game {@link Game} to be removed.
   */
  public void remove(@NonNull Game game) {
    gameRepository.delete(game);
  }

  /**
   * Returns a subset of {@link Game} instances from the collection, filtered by {@code
   * statusString}. If {@code statusString}, when converted to uppercase, matches one of the
   * enumerated {@link Status#values() values} of {@link Status}, the corresponding subset is
   * returned; otherwise {@link IllegalArgumentException} is thrown.
   *
   * @param statusString Filter keyword, matching (when converted to uppercase) one of {@link
   *                     Status#values()}.
   * @return Subset of codes collection, filtered by {@code statusString}.
   * @throws InvalidPropertyException If {@code statusString} does not match one of {@link
   *                                  Status#values()}.
   */
  public Iterable<Game> list(@NonNull String statusString) throws InvalidPropertyException {
    try {
      Status status = Status.valueOf(statusString.toUpperCase());
      Iterable<Game> selection;
      switch (status) {
        case ALL:
          selection = gameRepository.getAllByOrderByCreatedDesc();
          break;
        case UNSOLVED:
          selection = gameRepository.getAllUnsolvedOrderByCreatedDesc();
          break;
        case SOLVED:
          selection = gameRepository.getAllSolvedOrderByCreatedDesc();
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
    gameRepository.deleteAll();
  }

  private static boolean isInvalidCodePoint(int codePoint) {
    return !Character.isDefined(codePoint)
        || Character.isWhitespace(codePoint)
        || Character.isISOControl(codePoint);
  }

  /**
   * Allowed filter values for retrieving {@link Game} subsets from the collection.
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
