package com.andanza.backend.admin.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminCategoryRequest(
        @NotBlank(message = "El nombre de la categoría es obligatorio")
        @Size(max = 40)
        String name,

        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 200)
        String description
) {
}
