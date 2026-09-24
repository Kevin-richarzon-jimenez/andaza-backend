package com.andanza.backend.contact;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContactRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String name,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 255, message = "El correo no puede superar los 255 caracteres")
        String email,

        @NotBlank(message = "El asunto es obligatorio")
        @Size(max = 120, message = "El asunto no puede superar los 120 caracteres")
        String subject,

        @NotBlank(message = "El mensaje no puede estar vacío")
        @Size(min = 10, max = 1000, message = "El mensaje debe tener entre 10 y 1000 caracteres")
        String message
) {
}
