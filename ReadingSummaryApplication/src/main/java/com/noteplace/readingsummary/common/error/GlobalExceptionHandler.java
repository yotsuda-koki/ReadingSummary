package com.noteplace.readingsummary.common.error;

import java.util.Optional;

import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

private String traceId() { return Optional.ofNullable(MDC.get("traceId")).orElse("-"); }

@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
 String msg = ex.getBindingResult().getFieldErrors().stream()
   .findFirst().map(e -> e.getField() + ": " + e.getDefaultMessage()).orElse("validation error");
 return ResponseEntity.badRequest().body(new ApiError("VALIDATION_ERROR", msg, traceId()));
}

@ExceptionHandler(AccessDeniedException.class)
public ResponseEntity<ApiError> handleDenied(AccessDeniedException ex) {
 return ResponseEntity.status(403).body(new ApiError("ACCESS_DENIED", "forbidden", traceId()));
}

@ExceptionHandler(Exception.class)
public ResponseEntity<ApiError> handleAny(Exception ex) {
 return ResponseEntity.status(500).body(new ApiError("INTERNAL_ERROR", "unexpected error", traceId()));
}
}

