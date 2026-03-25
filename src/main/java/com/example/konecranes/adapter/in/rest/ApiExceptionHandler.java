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
 * Translates common exceptions thrown by REST endpoints into JSON error responses.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /**
     * Handles transport or process I/O failures.
     *
     * @param ex thrown exception
     * @return conflict response with standardized error payload
     */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<Map<String, Object>> handleIo(IOException ex) {
        return response(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * Handles request validation and argument contract failures.
     *
     * @param ex thrown exception
     * @return bad request response with standardized error payload
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return response(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> response(HttpStatus status, String message) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("timestamp", Instant.now().toString());
        payload.put("status", status.value());
        payload.put("error", status.getReasonPhrase());
        payload.put("message", message);
        return ResponseEntity.status(status).body(payload);
    }
}


