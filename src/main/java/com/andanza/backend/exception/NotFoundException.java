package com.andanza.backend.exception;

import org.springframework.http.HttpStatus;

// 404: el recurso que se pide por la ruta no existe (o no pertenece al usuario).
public class NotFoundException extends BusinessException {

    public NotFoundException(String field, String message) {
        super(HttpStatus.NOT_FOUND, field, message);
    }
}
