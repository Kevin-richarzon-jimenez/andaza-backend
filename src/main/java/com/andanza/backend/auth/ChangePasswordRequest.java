package com.andanza.backend.auth;

import com.andanza.backend.validation.FieldsMatch;
import com.andanza.backend.validation.StrongPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@FieldsMatch(field = "newPassword", confirmationField = "confirmNewPassword",
        message = "Las contraseñas nuevas no coinciden")
public record ChangePasswordRequest(
        @NotBlank(message = "Debes ingresar tu contraseña actual")
        String currentPassword,

        @NotBlank(message = "La nueva contraseña es obligatoria")
        @StrongPassword
        @Size(max = 72, message = "La contraseña no puede superar los 72 caracteres")
        String newPassword,

        @NotBlank(message = "Debes confirmar la nueva contraseña")
        String confirmNewPassword
) {
}
