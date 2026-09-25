package com.andanza.backend.storage;

import com.andanza.backend.exception.BusinessException;
import org.springframework.http.HttpStatus;

// El almacenamiento de archivos no está configurado (503) o falló al guardar o borrar (502).
public class StorageException extends BusinessException {

    public StorageException(HttpStatus status, String message) {
        super(status, "image", message);
    }
}
