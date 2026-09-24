package com.andanza.backend.exception;

import org.springframework.http.HttpStatus;

// 429: demasiados intentos fallidos seguidos (por ejemplo, en el login).
public class TooManyRequestsException extends BusinessException {

    public TooManyRequestsException(String field, String message) {
        super(HttpStatus.TOO_MANY_REQUESTS, field, message);
    }
}
