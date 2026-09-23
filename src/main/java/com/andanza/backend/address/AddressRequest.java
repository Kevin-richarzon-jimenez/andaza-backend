package com.andanza.backend.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @NotBlank(message = "El nombre de la dirección es obligatorio (ej: Casa, Oficina)")
        @Size(max = 40)
        String label,

        @NotBlank(message = "La ciudad es obligatoria")
        @Size(max = 60)
        String city,

        @NotBlank(message = "La dirección es obligatoria")
        @Size(max = 150)
        String address,

        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(regexp = "^(?=.*[0-9])[0-9+ ]{7,15}$", message = "El teléfono no tiene un formato válido")
        String phone,

        @Pattern(regexp = "^$|^[0-9]{6}$", message = "El código postal debe tener 6 dígitos")
        String postalCode // opcional
) {
}
