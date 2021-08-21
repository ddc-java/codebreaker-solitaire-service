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
package edu.cnm.deepdive.codebreaker.controller;

import edu.cnm.deepdive.codebreaker.controller.CodebreakerExceptionHandler.InvalidPropertyException;
import edu.cnm.deepdive.codebreaker.model.entity.Code;
import edu.cnm.deepdive.codebreaker.model.entity.Guess;
import edu.cnm.deepdive.codebreaker.service.CodeService;
import edu.cnm.deepdive.codebreaker.service.GuessService;
import java.util.NoSuchElementException;
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
 * to {@link Code}, the paths for all controller methods in this class are hierarchical, including
 * components and path variables to reference an instance of {@link Code}.
 */
@RestController
@RequestMapping(PathComponents.GUESSES_PATH)
@ExposesResourceFor(Guess.class)
@CrossOrigin({"http://localhost:4200"})
public class GuessController {

  private final CodeService codeService;
  private final GuessService guessService;

  /**
   * Initializes this instance with a {@link CodeService} and {@link GuessService}.
   *
   * @param codeService  Provider of high-level {@link Code}-related operations.
   * @param guessService Provider of high-level {@link Guess}-related operations.
   */
  @Autowired
  public GuessController(CodeService codeService, GuessService guessService) {
    this.codeService = codeService;
    this.guessService = guessService;
  }

  /**
   * Returns all {@link Guess} instances associated with the specified {@link Code}.
   *
   * @param codeId Unique identifier of code.
   * @return All guesses submitted against the specified {@link Code}, in descending order by
   * submission timestamp.
   * @throws NoSuchElementException If {@code codeId} does not refer to a known {@link Code}.
   */
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public Iterable<Guess> list(@PathVariable String codeId) throws NoSuchElementException {
    return codeService
        .get(codeId)
        .map(Code::getGuesses)
        .orElseThrow();
  }

  /**
   * Adds the specified {@link Guess} to the referenced {@link Code code}'s collection of guesses.
   *
   * @param codeId Unique identifier of code.
   * @param guess  {@link Guess} submitted against {@link Code} referenced by {@code codeId}.
   * @return Validated and persisted {@link Guess} instance.
   * @throws NoSuchElementException          If {@code codeId} does not refer to a known {@link
   *                                         Code}.
   * @throws MethodArgumentNotValidException If the {@code guess} properties fail low-level
   *                                         validation for data model integrity.
   * @throws InvalidPropertyException        If the {@code guess} properties fail high-level
   *                                         validation against business rules.
   */
  @PostMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Guess> post(@PathVariable String codeId, @Valid @RequestBody Guess guess)
      throws NoSuchElementException, MethodArgumentNotValidException, InvalidPropertyException {
    return codeService
        .get(codeId)
        .map((code) -> guessService.add(code, guess))
        .map((g) -> ResponseEntity.created(g.getHref()).body(g))
        .orElseThrow();
  }

  /**
   * Returns a single instance of {@link Guess}, as specified by {@code codeId} and {@code guessId}.
   * If the {@link Guess} referenced by {@code guessId} is not one of the guesses submitted against
   * the {@link Code} referenced by {@code codeId}, {@link NoSuchElementException} is thrown.
   *
   * @param codeId  Unique identifier of code.
   * @param guessId Unique identifier of guess.
   * @return {@link Guess} referenced by {@code guessId}.
   * @throws NoSuchElementException If the referenced {@link Code} does not exist, the referenced
   *                                {@link Guess} does not exist, or the {@link Code} was not
   *                                submitted against the specified {@link Code}.
   */
  @GetMapping(value = PathComponents.GUESS_ID_COMPONENT,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Guess get(@PathVariable String codeId, @PathVariable String guessId)
      throws NoSuchElementException {
    return codeService
        .get(codeId)
        .flatMap((code) -> guessService.get(guessId))
        .orElseThrow();
  }

}
