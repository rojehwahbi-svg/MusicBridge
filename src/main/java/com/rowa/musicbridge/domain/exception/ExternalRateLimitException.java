package com.rowa.musicbridge.domain.exception;

public class ExternalRateLimitException extends RuntimeException {
    public ExternalRateLimitException(String message) {
        super(message);
    }
}
