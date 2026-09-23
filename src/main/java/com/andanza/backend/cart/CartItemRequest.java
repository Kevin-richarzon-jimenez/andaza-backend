package com.andanza.backend.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CartItemRequest(
        @NotBlank(message = "El producto es obligatorio")
        String productId,

        @NotBlank(message = "Selecciona una talla")
        String size,

        @NotBlank(message = "Selecciona un color")
        String color,

        @Min(value = 1, message = "La cantidad mínima es 1")
        int quantity
) {
}
