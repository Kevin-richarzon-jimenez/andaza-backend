package com.andanza.backend.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            String message = fe.isBindingFailure() ? "El valor ingresado no es válido" : fe.getDefaultMessage();
            errors.put(fe.getField(), message);
        }
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Datos inválidos",
                "Revisa los campos del formulario",
                errors
        );
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJson(HttpMessageNotReadableException ex) {
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(), "Datos inválidos", "El cuerpo de la petición no es un JSON válido");
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(), "Datos inválidos", "El valor del parámetro '" + ex.getName() + "' no es válido",
                Map.of(ex.getName(), "El valor ingresado no es válido"));
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex) {
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(), "Datos inválidos", "Falta el parámetro '" + ex.getParameterName() + "'",
                Map.of(ex.getParameterName(), "Este parámetro es obligatorio"));
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.METHOD_NOT_ALLOWED.value(), "Método no permitido", "Ese método HTTP no está permitido en esta ruta");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(body);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(), "Tipo de contenido no soportado", "El cuerpo de la petición debe enviarse como JSON");
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(body);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> handleMissingPart(MissingServletRequestPartException ex) {
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(), "Datos inválidos", "Falta el archivo '" + ex.getRequestPartName() + "'",
                Map.of(ex.getRequestPartName(), "Este archivo es obligatorio"));
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleUploadTooLarge(MaxUploadSizeExceededException ex) {
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.CONTENT_TOO_LARGE.value(), "Archivo demasiado grande", "La imagen pesa demasiado.",
                Map.of("image", "La imagen pesa demasiado"));
        return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE).body(body);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoResourceFoundException ex) {
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(), "No encontrado", "La ruta solicitada no existe");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {
        HttpStatus status = ex.getStatus();
        ErrorResponse body = ErrorResponse.of(
                status.value(), titleFor(status), ex.getMessage(), Map.of(ex.getField(), ex.getMessage()));
        return ResponseEntity.status(status).body(body);
    }

    // Red de seguridad para condiciones de carrera: las validaciones del servicio no alcanzan a
    // ver un duplicado que otra petición acaba de insertar y lo detecta la restricción de la base.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Violación de integridad de datos", ex);
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.CONFLICT.value(), "Conflicto", "La operación choca con datos que ya existen");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Error no controlado", ex);
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error interno",
                "Ocurrió un error inesperado. Intenta nuevamente."
        );
        return ResponseEntity.internalServerError().body(body);
    }

    private static String titleFor(HttpStatus status) {
        return switch (status) {
            case NOT_FOUND -> "No encontrado";
            case CONFLICT -> "Conflicto";
            case UNAUTHORIZED -> "No autenticado";
            case FORBIDDEN -> "Sin permiso";
            case TOO_MANY_REQUESTS -> "Demasiados intentos";
            case BAD_GATEWAY -> "Servicio externo";
            case SERVICE_UNAVAILABLE -> "Servicio no disponible";
            default -> "Regla de negocio";
        };
    }
}
