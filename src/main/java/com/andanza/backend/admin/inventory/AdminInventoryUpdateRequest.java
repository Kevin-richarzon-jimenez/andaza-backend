package com.andanza.backend.admin.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AdminInventoryUpdateRequest(
        @NotNull(message = "El stock es obligatorio")
        @Min(value = 0, message = "El stock no puede ser negativo")
        Integer stock
) {
}
