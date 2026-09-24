package com.andanza.backend.exception;

import org.springframework.http.HttpStatus;

// 409: la operación choca con el estado actual (por ejemplo, un correo que ya existe).
public class ConflictException extends BusinessException {

    public ConflictException(String field, String message) {
        super(HttpStatus.CONFLICT, field, message);
    }
}
