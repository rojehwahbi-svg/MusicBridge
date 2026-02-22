package com.rowa.musicbridge.apis.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        int status,
        String error,
        String message,
        LocalDateTime timestamp,
        String path,
        List<FieldError> fieldErrors
) {
    public ErrorResponse(int status, String error, String message, String path) {
        this(status, error, message, LocalDateTime.now(), path, null);
    }

    public ErrorResponse(int status, String error, String message, String path, List<FieldError> fieldErrors) {
        this(status, error, message, LocalDateTime.now(), path, fieldErrors);
    }

    public record FieldError(String field, String message) {}
}

