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
package edu.cnm.deepdive.codebreaker.controller;

import com.fasterxml.jackson.annotation.JsonView;
import edu.cnm.deepdive.codebreaker.controller.CodebreakerExceptionHandler.InvalidPropertyException;
import edu.cnm.deepdive.codebreaker.model.entity.Game;
import edu.cnm.deepdive.codebreaker.model.entity.Guess;
import edu.cnm.deepdive.codebreaker.service.GameService;
import edu.cnm.deepdive.codebreaker.service.GameService.Status;
import edu.cnm.deepdive.codebreaker.view.GameProjection;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles HTTP requests related to {@link Game} instances. (Requests for {@link Guess} instances
 * are handled by {@link GuessController}.)
 */
@RestController
@RequestMapping(PathComponents.GAMES_PATH)
@ExposesResourceFor(Game.class)
@CrossOrigin({"http://localhost:4200"})
public class GameController {

  private final GameService gameService;

  /**
   * Initializes this instance with a {@link GameService}.
   *
   * @param gameService Provider of high-level {@link Game}-related operations.
   */
  public GameController(GameService gameService) {
    this.gameService = gameService;
  }

  /**
   * Returns all {@link Game} instances with a state (solved or unsolved) matching {@code status}.
   * Note that this match is case-insensitive.
   *
   * @param status {@link String} value specifying the desired subset of games to query.
   * @return {@link Iterable Iterable&lt;Game&gt;} instances matching {@code status}, in descending
   * order by creation timestamp.
   * @throws InvalidPropertyException If {@code state} does not match one of the values of {@link
   *                                  Status}.
   */
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @JsonView(GameProjection.Simple.class)
  public Iterable<Game> list(@RequestParam(required = false, defaultValue = "ALL") String status)
      throws InvalidPropertyException {
    return gameService
        .list(status)
        .collect(Collectors.toList());
  }

  /**
   * Adds {@code game} to the system, setting any necessary properties (e.g. generating the secret
   * text), thereby starting a new game. Minimally, {@code game} must include the {@code pool} and
   * {@code length} properties; otherwise, the secret text of the code can't be generated.
   *
   * @param game {@link Game} specifying the character pool and length of the code to be generated.
   * @return Validated, completed, and persisted {@link Game} instance.
   *                                         validation for data model integrity.
   * @throws InvalidPropertyException        If the {@code game} properties fail high-level
   *                                         validation against business rules.
   */
  @PostMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @JsonView(GameProjection.Simple.class)
  public ResponseEntity<Game> post(@Valid @RequestBody Game game) throws InvalidPropertyException {
    game = gameService.add(game);
    return ResponseEntity
        .created(game.getHref())
        .body(game);
  }

  /**
   * Returns the single {@link Game} matching the specified {@code gameId}, if it exists.
   *
   * @param gameId Unique identifier of {@link Game} to be retrieved.
   * @return {@link Game} referenced by {@code gameId}.
   * @throws NoSuchElementException If {@code gameId} does not refer to a known {@link Game}.
   */
  @GetMapping(value = PathComponents.GAME_ID_COMPONENT,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @JsonView(GameProjection.Detailed.class)
  public Game get(@PathVariable UUID gameId) throws NoSuchElementException {
    return gameService
        .get(gameId)
        .orElseThrow();
  }

  /**
   * Deletes the single {@link Game} matching the specified {@code gameId}, if it exists.
   *
   * @param gameId Unique identifier of {@link Game} to be retrieved.
   * @throws NoSuchElementException If {@code gameId} does not refer to a known {@link Game}.
   */
  @DeleteMapping(value = PathComponents.GAME_ID_COMPONENT)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID gameId) throws NoSuchElementException {
    gameService
        .get(gameId)
        .ifPresentOrElse(
            gameService::remove,
            () -> {
              throw new NoSuchElementException();
            }
        );
  }

}
