package com.andanza.backend.exception;

import org.springframework.http.HttpStatus;

// 403: el usuario está autenticado pero no puede hacer esto (por ejemplo, cuenta bloqueada).
public class ForbiddenException extends BusinessException {

    public ForbiddenException(String field, String message) {
        super(HttpStatus.FORBIDDEN, field, message);
    }
}
