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
import edu.cnm.deepdive.codebreaker.service.CodeService.Status;
import java.net.URI;
import java.util.NoSuchElementException;
import javax.validation.Valid;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
 * Handles HTTP requests related to {@link Code} instances. (Requests for {@link Guess} instances
 * are handled by {@link GuessController}.)
 */
@RestController
@RequestMapping(PathComponents.CODES_PATH)
@CrossOrigin({"https://www.webtools.services", "http://localhost:4200"})
public class CodeController {

  private final CodeService codeService;

  /**
   * Initializes this instance with a {@link CodeService}.
   *
   * @param codeService Provider of high-level {@link Code}-related operations.
   */
  public CodeController(CodeService codeService) {
    this.codeService = codeService;
  }

  /**
   * Returns all {@link Code} instances with a state (solved or unsolved) matching {@code status}.
   * Note that this match is case-insensitive.
   *
   * @param status {@link String} value specifying the desired subset of codes to query.
   * @return {@link Iterable Iterable&lt;Code&gt;} instances matching {@code status}, in descending
   * order by creation timestamp.
   * @throws InvalidPropertyException If {@code state} does not match one of the values of {@link
   *                                  Status}.
   */
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public Iterable<Code> list(@RequestParam(required = false, defaultValue = "ALL") String status)
      throws InvalidPropertyException {
    return codeService.list(status);
  }

  /**
   * Adds {@code code} to the system, setting any necessary properties (e.g. generating the secret
   * text), thereby starting a new game. Minimally, {@code code} must include the {@code pool} and
   * {@code length} properties; otherwise, the secret text of the code can't be generated.
   *
   * @param code {@link Code} specifying the character pool and length of the code to be generated.
   * @return Validated, completed, and persisted {@link Code} instance.
   * @throws MethodArgumentNotValidException If the {@code code} properties fail low-level
   *                                         validation for data model integrity.
   * @throws InvalidPropertyException        If the {@code code} properties fail high-level
   *                                         validation against business rules.
   */
  @PostMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Code> post(@Valid @RequestBody Code code)
      throws MethodArgumentNotValidException, InvalidPropertyException {
    code = codeService.add(code);
    URI location = WebMvcLinkBuilder
        .linkTo(WebMvcLinkBuilder
            .methodOn(CodeController.class)
            .get(code.getKey()))
        .toUri();
    return ResponseEntity.created(location).body(code);
  }

  /**
   * Returns the single {@link Code} matching the specified {@code codeId}, if it exists.
   *
   * @param codeId Unique identifier of {@link Code} to be retrieved.
   * @return {@link Code} referenced by {@code codeId}.
   * @throws NoSuchElementException If {@code codeId} does not refer to a known {@link Code}.
   */
  @GetMapping(value = PathComponents.CODE_ID_COMPONENT,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Code get(@PathVariable String codeId) throws NoSuchElementException {
    return codeService
        .get(codeId)
        .orElseThrow();
  }

  /**
   * Deletes the single {@link Code} matching the specified {@code codeId}, if it exists.
   *
   * @param codeId Unique identifier of {@link Code} to be retrieved.
   * @throws NoSuchElementException If {@code codeId} does not refer to a known {@link Code}.
   */
  @DeleteMapping(value = PathComponents.CODE_ID_COMPONENT)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable String codeId) throws NoSuchElementException {
    codeService
        .get(codeId)
        .stream()
        .peek(codeService::remove)
        .findFirst()
        .orElseThrow();
  }

}
