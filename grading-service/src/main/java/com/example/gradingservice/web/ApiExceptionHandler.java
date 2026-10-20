package com.example.gradingservice.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/** Converts exceptions into a predictable JSON error contract. */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex,
                                                                HttpServletRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fields.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        Map<String, Object> body = errorBody(HttpStatus.BAD_REQUEST, "Validation failed",
                "One or more request fields are invalid.", request, fields);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex,
                                                                HttpServletRequest request) {
        return ResponseEntity.badRequest()
                .body(errorBody(HttpStatus.BAD_REQUEST, "Bad request", ex.getMessage(), request, null));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NoResourceFoundException ex,
                                                              HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(errorBody(HttpStatus.NOT_FOUND, "Not found",
                        "The requested grading-service resource does not exist.", request, null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(Exception ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorBody(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error",
                        "The grading service could not complete the request.", request, null));
    }

    private Map<String, Object> errorBody(HttpStatus status, String error, String message,
                                          HttpServletRequest request, Map<String, String> fieldErrors) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        body.put("path", request.getRequestURI());
        if (fieldErrors != null && !fieldErrors.isEmpty()) {
            body.put("fieldErrors", fieldErrors);
        }
        return body;
    }
}
