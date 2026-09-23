package com.andanza.backend.exception;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        List<FieldDetail> errors
) {
    public record FieldDetail(String field, String message) {}

    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(Instant.now(), status, error, message, List.of());
    }

    public static ErrorResponse of(int status, String error, String message, Map<String, String> fieldErrors) {
        List<FieldDetail> details = fieldErrors.entrySet().stream()
                .map(e -> new FieldDetail(e.getKey(), e.getValue()))
                .toList();
        return new ErrorResponse(Instant.now(), status, error, message, details);
    }
}
