package com.rowa.musicbridge.apis.controller;

import com.rowa.musicbridge.apis.dto.ErrorResponse;
import com.rowa.musicbridge.domain.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Global exception handler for all REST controllers in the application.
 * <p>
 * This class uses {@link RestControllerAdvice} to intercept exceptions thrown by any controller
 * and convert them into standardized {@link ErrorResponse} objects with appropriate HTTP status codes.
 * </p>
 * <p>
 * The handler covers the following exception types:
 * <ul>
 *   <li>{@link ResourceNotFoundException} - Returns 404 Not Found</li>
 *   <li>{@link ResourceConflictException} - Returns 409 Conflict</li>
 *   <li>{@link MethodArgumentNotValidException} - Returns 400 Bad Request with field validation errors</li>
 *   <li>{@link ExternalApiException} - Returns 502 Bad Gateway</li>
 *   <li>{@link ExternalRateLimitException} - Returns 429 Too Many Requests</li>
 *   <li>{@link ExternalServiceUnavailableException} - Returns 503 Service Unavailable</li>
 *   <li>{@link IllegalArgumentException} - Returns 400 Bad Request</li>
 *   <li>{@link Exception} - Returns 500 Internal Server Error (fallback handler)</li>
 * </ul>
 * </p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles {@link ResourceNotFoundException} when a requested resource cannot be found.
     *
     * @param ex      the exception containing the error message
     * @param request the HTTP request that triggered the exception
     * @return a {@link ResponseEntity} with HTTP status 404 and an {@link ErrorResponse} body
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handles {@link ResourceConflictException} when a resource conflict occurs,
     * such as attempting to create a duplicate resource.
     *
     * @param ex      the exception containing the conflict details
     * @param request the HTTP request that triggered the exception
     * @return a {@link ResponseEntity} with HTTP status 409 and an {@link ErrorResponse} body
     */
    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ErrorResponse> handleResourceConflict(
            ResourceConflictException ex, HttpServletRequest request) {
        log.warn("Resource conflict: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Handles {@link MethodArgumentNotValidException} when request body validation fails.
     * <p>
     * This method extracts all field validation errors and includes them in the response,
     * allowing clients to display specific error messages for each invalid field.
     * </p>
     *
     * @param ex      the exception containing validation error details
     * @param request the HTTP request that triggered the exception
     * @return a {@link ResponseEntity} with HTTP status 400 and an {@link ErrorResponse} body
     *         containing a list of field-specific validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Validation failed: {}", ex.getMessage());
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ErrorResponse.FieldError(
                        error.getField(),
                        error.getDefaultMessage()))
                .toList();

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "One or more fields have validation errors",
                request.getRequestURI(),
                fieldErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handles {@link ExternalApiException} when communication with an external API fails.
     * <p>
     * This handler is triggered when there are issues communicating with external services
     * such as the Tidal API.
     * </p>
     *
     * @param ex      the exception containing the external API error details
     * @param request the HTTP request that triggered the exception
     * @return a {@link ResponseEntity} with HTTP status 502 and an {@link ErrorResponse} body
     */
    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ErrorResponse> handleExternalApiException(
            ExternalApiException ex, HttpServletRequest request) {
        log.error("External API error: {}", ex.getMessage(), ex);
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_GATEWAY.value(),
                "External API Error",
                "Error communicating with external service",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(error);
    }

    /**
     * Handles {@link ExternalRateLimitException} when the rate limit for an external API is exceeded.
     * <p>
     * This handler is triggered when too many requests have been made to an external service
     * within a certain time period.
     * </p>
     *
     * @param ex      the exception containing the rate limit error details
     * @param request the HTTP request that triggered the exception
     * @return a {@link ResponseEntity} with HTTP status 429 and an {@link ErrorResponse} body
     */
    @ExceptionHandler(ExternalRateLimitException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitException(
            ExternalRateLimitException ex, HttpServletRequest request) {
        log.warn("Rate limit exceeded: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                "Rate Limit Exceeded",
                "Too many requests to external service, please try again later",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
    }

    /**
     * Handles {@link ExternalServiceUnavailableException} when an external service is unavailable.
     * <p>
     * This handler is triggered when an external service (e.g., Tidal API) is temporarily
     * unavailable or experiencing downtime.
     * </p>
     *
     * @param ex      the exception containing the service unavailability details
     * @param request the HTTP request that triggered the exception
     * @return a {@link ResponseEntity} with HTTP status 503 and an {@link ErrorResponse} body
     */
    @ExceptionHandler(ExternalServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleServiceUnavailable(
            ExternalServiceUnavailableException ex, HttpServletRequest request) {
        log.error("External service unavailable: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Service Unavailable",
                "External service is temporarily unavailable, please try again later",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    /**
     * Handles {@link IllegalArgumentException} when an invalid argument is provided.
     * <p>
     * This handler is triggered when a method receives an argument that violates
     * its preconditions or business rules.
     * </p>
     *
     * @param ex      the exception containing the invalid argument details
     * @param request the HTTP request that triggered the exception
     * @return a {@link ResponseEntity} with HTTP status 400 and an {@link ErrorResponse} body
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Illegal argument: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Fallback handler for any unhandled exceptions.
     * <p>
     * This handler catches all exceptions that are not handled by more specific handlers.
     * It logs the full stack trace for debugging purposes and returns a generic error message
     * to avoid exposing sensitive internal details to the client.
     * </p>
     *
     * @param ex      the exception that was thrown
     * @param request the HTTP request that triggered the exception
     * @return a {@link ResponseEntity} with HTTP status 500 and an {@link ErrorResponse} body
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(
            Exception ex, HttpServletRequest request) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Unexpected error occurred",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Handles {@link MissingServletRequestParameterException} when a required request parameter is missing.
     * <p>
     * This handler is triggered when a client fails to provide a required query parameter or form parameter
     * in the request. It returns a clear error message indicating which parameter is missing.
     * </p>
     *
     * @param ex      the exception containing details about the missing parameter
     * @param request the HTTP request that triggered the exception
     * @return a {@link ResponseEntity} with HTTP status 400 and an {@link ErrorResponse} body
     */
    public ResponseEntity<ErrorResponse>  handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        log.warn("Missing request parameter: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

}

