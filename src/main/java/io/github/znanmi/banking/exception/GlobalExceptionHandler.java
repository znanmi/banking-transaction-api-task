package io.github.znanmi.banking.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.github.znanmi.banking.dto.ErrorResponseDTO;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(AccountNotFoundException.class)
  public ResponseEntity<ErrorResponseDTO> handleAccountNotFound(AccountNotFoundException ex) {
    return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), null);
  }

  @ExceptionHandler(InsufficientFundsException.class)
  public ResponseEntity<ErrorResponseDTO> handleInsufficientFunds(InsufficientFundsException ex) {
    return buildResponse(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage(), null);
  }

  @ExceptionHandler(SameAccountTransferException.class)
  public ResponseEntity<ErrorResponseDTO> handleSameAccount(SameAccountTransferException ex) {
    return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponseDTO> handleValidation(MethodArgumentNotValidException ex) {
    Map<String, String> fieldErrors = new LinkedHashMap<>();
    for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
      fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
    }
    return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", fieldErrors);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponseDTO> handleMalformedJson(HttpMessageNotReadableException ex) {
    return buildResponse(HttpStatus.BAD_REQUEST, "Request body is malformed or has invalid values", null);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponseDTO> handleUnexpected(Exception ex) {
    if (ex instanceof ErrorResponse springError) {
      HttpStatus status = HttpStatus.valueOf(springError.getStatusCode().value());
      return buildResponse(status, status.getReasonPhrase(), null);
    }
    log.error("Unexpected error", ex);
    return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", null);
  }

  private ResponseEntity<ErrorResponseDTO> buildResponse(HttpStatus status, String message,
      Map<String, String> fieldErrors) {
    ErrorResponseDTO body = new ErrorResponseDTO(
        status.value(),
        status.getReasonPhrase(),
        message,
        fieldErrors,
        Instant.now());
    return ResponseEntity.status(status).body(body);
  }
}
