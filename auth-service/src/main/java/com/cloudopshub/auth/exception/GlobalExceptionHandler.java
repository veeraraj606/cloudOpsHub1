package com.cloudopshub.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);


    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateResource(
            DuplicateResourceException ex){

        Map<String, Object> response = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.CONFLICT.value(),
                "error", "Conflict",
                "message",ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage())
                );

        Map<String, Object> response = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "Validation Failed",
                "errors", errors
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCredentials(
            InvalidCredentialsException ex) {

        Map<String, Object> response = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.UNAUTHORIZED.value(),
                "error", "Unauthorized",
                "message", ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(
            ResourceNotFoundException ex){
        Map<String,Object> response = Map.of(
                "timestamp", LocalDateTime.now(),
                "status" , HttpStatus.NOT_FOUND.value(),
                "error" , "not found",
                "message" , ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);   // logs the real cause to your console — critical for debugging
        Map<String, Object> response = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "error", "Internal Server Error",
                "message", "Something went wrong"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

}
