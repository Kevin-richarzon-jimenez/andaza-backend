package com.andanza.backend.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CartItemRequest(
        @NotNull(message = "Selecciona una variante (color y talla) del producto")
        UUID variantId,

        @Min(value = 1, message = "La cantidad mínima es 1")
        int quantity
) {
}
