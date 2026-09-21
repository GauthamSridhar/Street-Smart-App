package com.shopapp.common;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class ApiErrors {
  private static final Logger log = LoggerFactory.getLogger(ApiErrors.class);

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetail> handle(Exception e, HttpServletRequest request) {
    HttpStatus status;
    String detail;
    if (e instanceof ApiException api) {
      status = api.status();
      detail = api.getMessage();
    } else if (e instanceof AccessDeniedException) {
      status = HttpStatus.FORBIDDEN;
      detail = "Access denied";
    } else if (e instanceof AuthenticationException) {
      status = HttpStatus.UNAUTHORIZED;
      detail = "Invalid credentials";
    } else if (e instanceof DataIntegrityViolationException
        || e instanceof ObjectOptimisticLockingFailureException) {
      status = HttpStatus.CONFLICT;
      detail = "The request conflicts with existing data; refresh and retry";
    } else if (e instanceof MethodArgumentNotValidException invalid) {
      status = HttpStatus.BAD_REQUEST;
      detail =
          invalid.getBindingResult().getFieldErrors().stream()
              .map(f -> f.getField() + ": " + f.getDefaultMessage())
              .distinct()
              .sorted()
              .collect(java.util.stream.Collectors.joining("; "));
    } else if (e instanceof IllegalArgumentException
        || e instanceof ConstraintViolationException
        || e instanceof MethodArgumentTypeMismatchException
        || e instanceof HttpMessageNotReadableException
        || e instanceof org.springframework.web.bind.MissingServletRequestParameterException) {
      status = HttpStatus.BAD_REQUEST;
      detail = "Invalid request";
    } else if (e instanceof MaxUploadSizeExceededException) {
      status = HttpStatus.PAYLOAD_TOO_LARGE;
      detail = "Upload exceeds the allowed size";
    } else if (e instanceof FeignException remote) {
      status =
          switch (remote.status()) {
            case 400 -> HttpStatus.BAD_REQUEST;
            case 401 -> HttpStatus.UNAUTHORIZED;
            case 403 -> HttpStatus.FORBIDDEN;
            case 404 -> HttpStatus.NOT_FOUND;
            case 409 -> HttpStatus.CONFLICT;
            default -> HttpStatus.SERVICE_UNAVAILABLE;
          };
      detail =
          status == HttpStatus.SERVICE_UNAVAILABLE
              ? "A dependent service is temporarily unavailable"
              : "Related resource: " + status.getReasonPhrase();
    } else if (e instanceof org.springframework.web.ErrorResponse err) {
      status = HttpStatus.valueOf(err.getStatusCode().value());
      detail = status.getReasonPhrase();
    } else {
      status = HttpStatus.INTERNAL_SERVER_ERROR;
      detail = "An unexpected error occurred";
      log.error("Request failed: {}", request.getRequestURI(), e);
    }
    var problem = ProblemDetail.forStatusAndDetail(status, detail);
    problem.setProperty("requestId", request.getAttribute("requestId"));
    return ResponseEntity.status(status).body(problem);
  }
}
