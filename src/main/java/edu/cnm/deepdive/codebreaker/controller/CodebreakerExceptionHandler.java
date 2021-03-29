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

@RestControllerAdvice
public class CodebreakerExceptionHandler {

  private static final String ALREADY_SOLVED_MESSAGE = "Already solved";
  private static final String NOT_FOUND_MESSAGE = "Not found";
  private static final String VALIDATION_FAILURE_MESSAGE = "Validation failure";
  private static final String LENGTH_PROPERTY = "length";
  private static final String INVALID_LENGTH_MESSAGE = "must be an integer";

  @ExceptionHandler(NoSuchElementException.class)
  @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = NOT_FOUND_MESSAGE)
  public void handleNotFound() {
  }

  @ExceptionHandler(AlreadySolvedException.class)
  @ResponseStatus(value = HttpStatus.CONFLICT, reason = ALREADY_SOLVED_MESSAGE)
  public void handleAlreadSolved() {
  }

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

  public static class AlreadySolvedException extends IllegalStateException {
  }

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
