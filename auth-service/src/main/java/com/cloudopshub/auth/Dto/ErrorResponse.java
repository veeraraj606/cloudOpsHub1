package com.cloudopshub.auth.Dto;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
public class ErrorResponse {

    private final LocalDateTime timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final Map<String, String> errors; // only populated for validation failures

    public ErrorResponse(int status, String error, String message) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.errors = null;
    }

    public ErrorResponse(int status, String error, String message, Map<String, String> errors) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.errors = errors;
    }
}