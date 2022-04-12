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

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import edu.cnm.deepdive.codebreaker.model.entity.Game;
import edu.cnm.deepdive.codebreaker.view.UUIDStringifier.DecodeException;
import java.util.Date;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Defines several mappings of exception types (thrown by the methods in {@link GameController} and
 * {@link GuessController}) to HTTP response statuses. For some of these, the response body is an
 * instance of {@link DetailedExceptionResponse}.
 */
@RestControllerAdvice
public class CodebreakerExceptionHandler {

  private static final String ALREADY_SOLVED_MESSAGE = "Already solved";
  private static final String NOT_FOUND_MESSAGE = "Not found";
  private static final String VALIDATION_FAILURE_MESSAGE = "Validation failure";
  private static final String ILLEGAL_ARGUMENT_MESSAGE = "Invalid request content";
  private static final String LENGTH_PROPERTY = "length";
  private static final String INVALID_LENGTH_MESSAGE = "must be an integer";

  /**
   * Maps instances of {@link NoSuchElementException} and {@link DecodeException} to the HTTP 404
   * (not found) response status. The inclusion of the latter is due to the fact that the only point
   * at which the application decodes UUIDs is on parsing path variables in a URL; thus, an error in
   * decoding should be treated as a request for a nonexistent resource.
   */
  @ExceptionHandler({NoSuchElementException.class, DecodeException.class})
  @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = NOT_FOUND_MESSAGE)
  public void handleNotFound() {
  }

  /**
   * Maps {@link AlreadySolvedException} (thrown when a guess is submitted for a code that's already
   * been solved) to the HTTP 409 (conflict) response status.
   */
  @ExceptionHandler(AlreadySolvedException.class)
  @ResponseStatus(value = HttpStatus.CONFLICT, reason = ALREADY_SOLVED_MESSAGE)
  public void handleAlreadySolved() {
  }

  /**
   * Maps {@link MethodArgumentNotValidException} (thrown when one of the {@link javax.validation}
   * conditions, declared on fields of {@link Game} and {@link edu.cnm.deepdive.codebreaker.model.entity.Guess},
   * fails) to the HTTP 400 (bad request) response status, then constructs and returns a response
   * body with details on the failure.
   *
   * @param ex      {@link MethodArgumentNotValidException} thrown by one of the {@link
   *                javax.validation} conditions.
   * @param request {@link HttpServletRequest} containing the request that failed validation.
   * @return {@link DetailedExceptionResponse} with details extracted from {@code ex}.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public DetailedExceptionResponse handleGeneralValidationFailure(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    //noinspection ConstantConditions
    return new DetailedExceptionResponse(
        HttpStatus.BAD_REQUEST,
        VALIDATION_FAILURE_MESSAGE,
        ex.getBindingResult()
            .getAllErrors()
            .stream()
            .collect(Collectors.toMap((error) -> ((FieldError) error).getField(),
                ObjectError::getDefaultMessage)),
        request
    );
  }

  /**
   * Maps {@link InvalidPropertyException} (thrown when high-level validation of fields of {@link
   * Game} or {@link edu.cnm.deepdive.codebreaker.model.entity.Guess}, fails) to the HTTP 400 (bad
   * request) response status, then constructs and returns a response body with details on the
   * failure.
   *
   * @param ex      {@link InvalidPropertyException} thrown by violation of high-level business
   *                rules.
   * @param request {@link HttpServletRequest} containing the request that failed validation.
   * @return {@link DetailedExceptionResponse} with details extracted from {@code ex}.
   */
  @ExceptionHandler(InvalidPropertyException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public DetailedExceptionResponse handlePropertyValidationFailure(
      InvalidPropertyException ex, HttpServletRequest request) {
    return new DetailedExceptionResponse(
        HttpStatus.BAD_REQUEST,
        VALIDATION_FAILURE_MESSAGE,
        Map.of(ex.getProperty(), ex.getMessage()),
        request
    );
  }

  /**
   * Maps {@link MismatchedInputException} (thrown when deserialization of a floating-point value to
   * an integer-valued field is attempted) to the HTTP 400 (bad request) response status, then
   * constructs and returns a response body with details on the failure.
   *
   * @param ex      {@link MismatchedInputException} thrown on attempted deserialization.
   * @param request {@link HttpServletRequest} containing the request with one or more invalid
   *                floating-point values in the request body.
   * @return {@link DetailedExceptionResponse} with details extracted from {@code ex}.
   */
  @ExceptionHandler(MismatchedInputException.class)
  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  public DetailedExceptionResponse handleMismatchedInput(
      MismatchedInputException ex, HttpServletRequest request) {
    return new DetailedExceptionResponse(
        HttpStatus.BAD_REQUEST,
        VALIDATION_FAILURE_MESSAGE,
        Map.of(LENGTH_PROPERTY, INVALID_LENGTH_MESSAGE),
        request
    );
  }

  /**
   * Maps any {@link IllegalArgumentException} instances not of the {@link
   * edu.cnm.deepdive.codebreaker.view.UUIDStringifier.DecodeException}, {@link
   * MethodArgumentNotValidException}, {@link InvalidPropertyException}, or {@link
   * MismatchedInputException} subclasses to the HTTP 400 (bad request) response status.
   */
  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = ILLEGAL_ARGUMENT_MESSAGE)
  public void handleIllegalArgument() {
  }

  /**
   * Defines a subclass of {@link IllegalStateException}, for use when a {@link
   * edu.cnm.deepdive.codebreaker.model.entity.Guess} is submitted for a {@link Game} that is
   * already solved.
   */
  public static class AlreadySolvedException extends IllegalStateException {

  }

  /**
   * Defines a subclass of {@link IllegalArgumentException}, for use when a {@link Game} or {@link
   * edu.cnm.deepdive.codebreaker.model.entity.Guess} is submitted with properties that violate
   * high-level business rules.
   */
  public static class InvalidPropertyException extends IllegalArgumentException {

    private final String property;
    private final String message;

    /**
     * Initializes this instance with the specified property name and detailed message.
     *
     * @param property Name of property with an invalid value.
     * @param message Description of validation failure.
     */
    public InvalidPropertyException(String property, String message) {
      this.property = property;
      this.message = message;
    }

    /**
     * Returns Name of property failing validation check.
     *
     * @return
     */
    public String getProperty() {
      return property;
    }

    @Override
    public String getMessage() {
      return message;
    }
  }

  /**
   * Encapsulates the properties included in the response body that's returned when an exception
   * occurs requiring more detail than the default exception response.
   */
  public static class DetailedExceptionResponse {

    private final Date timestamp;
    private final int status;
    private final String message;
    private final String error;
    private final String path;
    private final Map<String, String> details;

    /**
     * Initializes this error payload object with the specified properties.
     *
     * @param status  {@link HttpStatus} containing the error code.
     * @param message General error text.
     * @param details Request-specific properties.
     * @param request {@link HttpServletRequest} from which exception was thrown.
     */
    public DetailedExceptionResponse(HttpStatus status, String message, Map<String, String> details,
        HttpServletRequest request) {
      timestamp = new Date();
      this.status = status.value();
      this.message = message;
      error = status.getReasonPhrase();
      path = request.getRequestURI();
      this.details = details;
    }

    /**
     * Returns the timestamp of the error.
     *
     * @return
     */
    public Date getTimestamp() {
      return timestamp;
    }

    /**
     * Returns the HTTP response status code. indicating the general error type.
     *
     * @return
     */
    public int getStatus() {
      return status;
    }

    /**
     * Returns a short text description of the general error type.
     *
     * @return
     */
    public String getMessage() {
      return message;
    }

    /**
     * Returns a summary description of the specific error.
     *
     * @return
     */
    public String getError() {
      return error;
    }

    /**
     * Returns the host-relative path portion of the requested URL.
     *
     * @return
     */
    public String getPath() {
      return path;
    }

    /**
     * Returns details of the error(s), as a {@link Map}&lt;{@link String}, {@link String}&gt;,
     * where each key is a request property name, and the associated value is the specific issue
     * with the property.
     *
     * @return Property-to-error {@link Map}.
     */
    public Map<String, String> getDetails() {
      return details;
    }

  }

}
