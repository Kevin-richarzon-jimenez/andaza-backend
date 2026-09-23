package com.andanza.backend.auth;

import com.andanza.backend.validation.FieldsMatch;
import com.andanza.backend.validation.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@FieldsMatch(field = "password", confirmationField = "confirmPassword",
        message = "Las contraseñas no coinciden")
public record RegisterRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 40, message = "El nombre debe tener entre 2 y 40 caracteres")
        String firstName,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(min = 2, max = 40, message = "El apellido debe tener entre 2 y 40 caracteres")
        String lastName,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @StrongPassword
        String password,

        @NotBlank(message = "Debes confirmar la contraseña")
        String confirmPassword
) {
}
