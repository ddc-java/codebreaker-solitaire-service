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
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;
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
   * Removes all games from the collection.
   */
  public void clear() {
    gameRepository.deleteAll();
  }

  private static boolean isInvalidCodePoint(int codePoint) {
    return !Character.isDefined(codePoint)
        || Character.isWhitespace(codePoint)
        || Character.isISOControl(codePoint);
  }

}
