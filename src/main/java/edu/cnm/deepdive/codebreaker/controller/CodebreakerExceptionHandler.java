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

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
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
 * Defines several mappings of exception types (thrown by the methods in {@link CodeController} and
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
   * Maps {@link NoSuchElementException} to the HTTP 404 (not found) response status.
   */
  @ExceptionHandler(NoSuchElementException.class)
  @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = NOT_FOUND_MESSAGE)
  public void handleNotFound() {
  }

  /**
   * Maps {@link AlreadySolvedException} (thrown when a guess is submitted for a code that's already
   * been solved) to the HTTP 409 (conflict) response status.
   */
  @ExceptionHandler(AlreadySolvedException.class)
  @ResponseStatus(value = HttpStatus.CONFLICT, reason = ALREADY_SOLVED_MESSAGE)
  public void handleAlreadSolved() {
  }

  /**
   * Maps {@link MethodArgumentNotValidException} (thrown when one of the {@link javax.validation}
   * conditions, declared on fields of {@link edu.cnm.deepdive.codebreaker.model.entity.Code} and
   * {@link edu.cnm.deepdive.codebreaker.model.entity.Guess}, fails) to the HTTP 400 (bad request)
   * response status, then constructs and returns a response body with details on the failure.
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
   * edu.cnm.deepdive.codebreaker.model.entity.Code} or {@link edu.cnm.deepdive.codebreaker.model.entity.Guess},
   * fails) to the HTTP 400 (bad request) response status, then constructs and returns a response
   * body with details on the failure.
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
        Map.of(ex.getText(), ex.getMessage()),
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
   * MethodArgumentNotValidException}, {@link InvalidPropertyException}, or {@link
   * MismatchedInputException} subclasses to the HTTP 400 (bad request) response status.
   */
  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = ILLEGAL_ARGUMENT_MESSAGE)
  public void handleIllegalArgument() {
  }

  /**
   * Defines a subclass of {@link IllegalStateException}, for use when a {@link
   * edu.cnm.deepdive.codebreaker.model.entity.Guess} is submitted for a {@link
   * edu.cnm.deepdive.codebreaker.model.entity.Code} that is already solved.
   */
  public static class AlreadySolvedException extends IllegalStateException {

  }

  /**
   * Defines a subclass of {@link IllegalArgumentException}, for use when a {@link
   * edu.cnm.deepdive.codebreaker.model.entity.Code} or {@link
   * edu.cnm.deepdive.codebreaker.model.entity.Guess} is submitted with properties that violate
   * high-level business rules.
   */
  public static class InvalidPropertyException extends IllegalArgumentException {

    private final String text;
    private final String message;

    public InvalidPropertyException(String text, String message) {
      this.text = text;
      this.message = message;
    }

    public String getText() {
      return text;
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

    public DetailedExceptionResponse(HttpStatus status, String message, Map<String, String> details,
        HttpServletRequest request) {
      timestamp = new Date();
      this.status = status.value();
      this.message = message;
      error = status.getReasonPhrase();
      path = request.getRequestURI();
      this.details = details;
    }

    public Date getTimestamp() {
      return timestamp;
    }

    public int getStatus() {
      return status;
    }

    public String getMessage() {
      return message;
    }

    public String getError() {
      return error;
    }

    public String getPath() {
      return path;
    }

    public Map<String, String> getDetails() {
      return details;
    }

  }

}
