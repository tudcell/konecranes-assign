package com.example.konecranes.adapter.in.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Global exception handler for REST controllers.
 *
 * Converts common exceptions into consistent JSON error responses
 * so controllers do not need to build error payloads manually.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /**
     * Handles I/O-related failures.
     *
     * Typical cases in this project:
     * - failed vehicle communication
     * - missing active connection
     * - process or transport write failure
     *
     * Returns HTTP 409 because the request may be valid,
     * but the operation cannot be completed in the current system state.
     *
     * @param ex thrown exception
     * @return conflict response with standard error payload
     */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<Map<String, Object>> handleIo(IOException ex) {
        return response(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * Handles invalid arguments or business-contract violations.
     *
     * Returns HTTP 400 when the client sends a request
     * that contains invalid values for the requested operation.
     *
     * @param ex thrown exception
     * @return bad request response with standard error payload
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return response(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Builds a standard JSON error response.
     *
     * Payload fields:
     * - timestamp: time when the error response was created
     * - status: HTTP status code
     * - error: HTTP reason phrase
     * - message: error details
     *
     * @param status HTTP status to return
     * @param message human-readable error message
     * @return response entity containing structured error information
     */
    private ResponseEntity<Map<String, Object>> response(HttpStatus status, String message) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("timestamp", Instant.now().toString());
        payload.put("status", status.value());
        payload.put("error", status.getReasonPhrase());
        payload.put("message", message);
        return ResponseEntity.status(status).body(payload);
    }
}