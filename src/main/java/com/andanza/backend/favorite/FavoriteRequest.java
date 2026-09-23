package com.andanza.backend.favorite;

import jakarta.validation.constraints.NotBlank;

public record FavoriteRequest(
        @NotBlank(message = "El producto es obligatorio")
        String productId
) {
}
