package com.andanza.backend.admin.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AdminInventoryUpdateRequest(
        @NotBlank(message = "El producto es obligatorio")
        String productId,

        @NotBlank(message = "El color es obligatorio")
        String color,

        @NotBlank(message = "La talla es obligatoria")
        String size,

        @Min(value = 0, message = "El stock no puede ser negativo")
        int stock
) {
}
