package com.andanza.backend.exception;

import org.springframework.http.HttpStatus;

// Error de negocio con un campo asociado. Por defecto responde 422; los casos que
// corresponden a otro código usan las subclases (NotFoundException, ConflictException, ...).
public class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final String field;

    public BusinessException(String field, String message) {
        this(HttpStatus.UNPROCESSABLE_CONTENT, field, message);
    }

    protected BusinessException(HttpStatus status, String field, String message) {
        super(message);
        this.status = status;
        this.field = field;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getField() {
        return field;
    }
}
