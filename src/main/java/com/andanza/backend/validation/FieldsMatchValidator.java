package com.andanza.backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyDescriptor;
import java.util.Objects;

public class FieldsMatchValidator implements ConstraintValidator<FieldsMatch, Object> {

    private static final Logger log = LoggerFactory.getLogger(FieldsMatchValidator.class);

    private String field;
    private String confirmationField;
    private String message;

    @Override
    public void initialize(FieldsMatch annotation) {
        this.field = annotation.field();
        this.confirmationField = annotation.confirmationField();
        this.message = annotation.message();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // dejar que @NotNull en los campos individuales maneje esto
        }
        try {
            Object fieldValue = readProperty(value, field);
            Object confirmationValue = readProperty(value, confirmationField);

            boolean matches = Objects.equals(fieldValue, confirmationValue);
            if (!matches) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(message)
                        .addPropertyNode(confirmationField)
                        .addConstraintViolation();
            }
            return matches;
        } catch (Exception e) {
            // Si @FieldsMatch quedó mal configurado (nombre de campo que no
            // existe, etc.) NO lo dejamos pasar como válido -- es mejor
            // bloquear la petición y que quede registrado el error, que
            // aceptar en silencio una contraseña sin confirmar de verdad.
            log.error("No se pudo validar @FieldsMatch entre '{}' y '{}'", field, confirmationField, e);
            return false;
        }
    }

    private Object readProperty(Object object, String propertyName) throws Exception {
        // Soporta tanto records (métodos de acceso sin "get") como beans clásicos.
        try {
            return object.getClass().getMethod(propertyName).invoke(object);
        } catch (NoSuchMethodException e) {
            PropertyDescriptor pd = new PropertyDescriptor(propertyName, object.getClass());
            return pd.getReadMethod().invoke(object);
        }
    }
}
