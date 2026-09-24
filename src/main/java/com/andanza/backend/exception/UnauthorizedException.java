package com.andanza.backend.exception;

import org.springframework.http.HttpStatus;

// 401: credenciales incorrectas.
public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(String field, String message) {
        super(HttpStatus.UNAUTHORIZED, field, message);
    }
}
