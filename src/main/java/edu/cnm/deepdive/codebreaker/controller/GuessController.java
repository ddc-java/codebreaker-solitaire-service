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

import edu.cnm.deepdive.codebreaker.controller.CodebreakerExceptionHandler.InvalidPropertyException;
import edu.cnm.deepdive.codebreaker.model.entity.Game;
import edu.cnm.deepdive.codebreaker.model.entity.Guess;
import edu.cnm.deepdive.codebreaker.service.GameService;
import edu.cnm.deepdive.codebreaker.service.GuessService;
import java.net.URI;
import java.util.NoSuchElementException;
import java.util.UUID;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles HTTP requests related to {@link Guess} instances. Since {@link Guess} is a child entity
 * to {@link Game}, the paths for all controller methods in this class are hierarchical, including
 * components and path variables to reference an instance of {@link Game}.
 */
@RestController
@RequestMapping(PathComponents.GUESSES_PATH)
@ExposesResourceFor(Guess.class)
@CrossOrigin({"http://localhost:4200"})
public class GuessController {

  private final GameService gameService;
  private final GuessService guessService;

  /**
   * Initializes this instance with a {@link GameService} and {@link GuessService}.
   *
   * @param gameService  Provider of high-level {@link Game}-related operations.
   * @param guessService Provider of high-level {@link Guess}-related operations.
   */
  @Autowired
  public GuessController(GameService gameService, GuessService guessService) {
    this.gameService = gameService;
    this.guessService = guessService;
  }

  /**
   * Returns all {@link Guess} instances associated with the specified {@link Game}.
   *
   * @param gameId Unique identifier of code.
   * @return All guesses submitted against the specified {@link Game}, in descending order by
   * submission timestamp.
   * @throws NoSuchElementException If {@code gameId} does not refer to a known {@link Game}.
   */
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public Iterable<Guess> list(@PathVariable UUID gameId)
      throws NoSuchElementException {
    return gameService
        .get(gameId)
        .map(Game::getGuesses)
        .orElseThrow();
  }

  /**
   * Adds the specified {@link Guess} to the referenced {@link Game game}'s collection of guesses.
   *
   * @param gameId Unique identifier of game.
   * @param guess  {@link Guess} submitted against {@link Game} referenced by {@code gameId}.
   * @return Validated and persisted {@link Guess} instance.
   * @throws NoSuchElementException          If {@code gameId} does not refer to a known {@link
   *                                         Game}.
   * @throws MethodArgumentNotValidException If the {@code guess} properties fail low-level
   *                                         validation for data model integrity.
   * @throws InvalidPropertyException        If the {@code guess} properties fail high-level
   *                                         validation against business rules.
   */
  @PostMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Guess> post(
      @PathVariable UUID gameId, @Valid @RequestBody Guess guess)
      throws NoSuchElementException, MethodArgumentNotValidException, InvalidPropertyException {
    return gameService
        .get(gameId)
        .map((game) -> guessService.add(game, guess))
        .map((g) -> {
          URI location = WebMvcLinkBuilder
              .linkTo(
                  WebMvcLinkBuilder
                      .methodOn(GuessController.class)
                      .get(gameId, guess.getExternalKey())
              )
              .toUri();
          return ResponseEntity
              .created(location)
              .body(g);
        })
        .orElseThrow();
  }

  /**
   * Returns a single instance of {@link Guess}, as specified by {@code gameId} and {@code guessId}.
   * If the {@link Guess} referenced by {@code guessId} is not one of the guesses submitted against
   * the {@link Game} referenced by {@code gameId}, {@link NoSuchElementException} is thrown.
   *
   * @param gameId  Unique identifier of game.
   * @param guessId Unique identifier of guess.
   * @return {@link Guess} referenced by {@code guessId}.
   * @throws NoSuchElementException If the referenced {@link Game} does not exist, the referenced
   *                                {@link Guess} does not exist, or the {@link Game} was not
   *                                submitted against the specified {@link Game}.
   */
  @GetMapping(value = PathComponents.GUESS_ID_COMPONENT,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Guess get(@PathVariable UUID gameId, @PathVariable UUID guessId)
      throws NoSuchElementException {
    return gameService
        .get(gameId)
        .flatMap((game) -> guessService.get(game, guessId))
        .orElseThrow();
  }

}
