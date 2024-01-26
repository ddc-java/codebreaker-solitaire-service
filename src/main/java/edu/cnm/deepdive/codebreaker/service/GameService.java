/*
 *  Copyright 2024 CNM Ingenuity, Inc.
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
@SuppressWarnings("JavadocDeclaration")
@Service
public class GameService implements AbstractGameService {

  private static final String POOL_PROPERTY = "pool";
  private static final String INVALID_CHARACTER_MESSAGE =
      "must not contain whitespace, control, or undefined characters";

  private final GameRepository gameRepository;
  private final Random rng;

  /**
   * Initialize this instance by injecting the required {@link GameRepository} and {@link Random}.
   *
   * @param gameRepository
   * @param rng
   */
  @Autowired
  public GameService(GameRepository gameRepository, Random rng) {
    this.gameRepository = gameRepository;
    this.rng = rng;
  }

  @Override
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

  @Override
  public Optional<Game> get(@NonNull UUID externalKey) {
    try {
      return gameRepository.findByExternalKey(externalKey);
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  @Override
  public void remove(@NonNull Game game) {
    gameRepository.delete(game);
  }

  @Override
  public void clear() {
    gameRepository.deleteAll();
  }

  private static boolean isInvalidCodePoint(int codePoint) {
    return !Character.isDefined(codePoint)
        || Character.isWhitespace(codePoint)
        || Character.isISOControl(codePoint);
  }

}
