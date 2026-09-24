package com.andanza.backend.admin.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminCategoryRequest(
        @NotBlank(message = "El nombre de la categoría es obligatorio")
        @Size(max = 40, message = "El nombre de la categoría no puede superar los 40 caracteres")
        String name,

        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 200, message = "La descripción no puede superar los 200 caracteres")
        String description
) {
}
