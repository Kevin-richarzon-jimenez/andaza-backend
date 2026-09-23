package com.andanza.backend.cart;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CartCalculationRequest(
        @NotEmpty(message = "El carrito no puede estar vacío")
        @Valid
        List<CartItemRequest> items,

        String discountCode // opcional
) {
}
