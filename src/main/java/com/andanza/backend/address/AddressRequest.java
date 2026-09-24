package com.andanza.backend.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @NotBlank(message = "El nombre de la dirección es obligatorio (ej: Casa, Oficina)")
        @Size(max = 40, message = "El nombre de la dirección no puede superar los 40 caracteres")
        String label,

        @NotBlank(message = "El nombre de quien recibe es obligatorio")
        @Size(max = 80, message = "El nombre de quien recibe no puede superar los 80 caracteres")
        String recipientName,

        @NotBlank(message = "La dirección es obligatoria")
        @Size(max = 150, message = "La dirección no puede superar los 150 caracteres")
        String street,

        @NotBlank(message = "La ciudad es obligatoria")
        @Size(max = 60, message = "La ciudad no puede superar los 60 caracteres")
        String city,

        @NotBlank(message = "El departamento es obligatorio")
        @Size(max = 60, message = "El departamento no puede superar los 60 caracteres")
        String department,

        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(regexp = "^(?=.*[0-9])[0-9+ ]{7,15}$", message = "El teléfono no tiene un formato válido")
        String phone,

        @Pattern(regexp = "^$|^[0-9]{6}$", message = "El código postal debe tener 6 dígitos")
        String postalCode,      // opcional

        Boolean isDefault       // opcional: true la deja como predeterminada; la primera dirección del usuario lo es siempre
) {
}
