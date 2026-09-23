package com.andanza.backend.admin.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record AdminProductRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100)
        String name,

        @NotBlank(message = "La marca es obligatoria")
        String brand,

        @NotBlank(message = "La categoría es obligatoria")
        String category,

        @NotNull(message = "El precio es obligatorio")
        @Positive(message = "El precio debe ser mayor a 0")
        BigDecimal price,

        @NotBlank(message = "La descripción es obligatoria")
        @Size(min = 10, max = 1000, message = "La descripción debe tener entre 10 y 1000 caracteres")
        String description,

        @NotEmpty(message = "Debes agregar al menos una variante de color")
        @Valid
        List<ColorVariantRequest> variants
) {
}
