package com.andanza.backend.admin.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Una variante: una combinación concreta de color + talla con su stock inicial.
public record ProductVariantRequest(
        @NotBlank(message = "El color de la variante es obligatorio")
        @Size(max = 40, message = "El color no puede superar los 40 caracteres")
        String color,

        @NotBlank(message = "La talla de la variante es obligatoria")
        @Size(max = 10, message = "La talla no puede superar los 10 caracteres")
        String size,

        @NotNull(message = "El stock de la variante es obligatorio")
        @Min(value = 0, message = "El stock no puede ser negativo")
        Integer stock
) {
}
