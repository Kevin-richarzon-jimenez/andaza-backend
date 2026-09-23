package com.andanza.backend.admin.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ColorVariantRequest(
        @NotBlank(message = "El color de la variante es obligatorio")
        String color,

        @NotEmpty(message = "La variante debe tener al menos una talla seleccionada")
        List<String> sizes
) {
}
