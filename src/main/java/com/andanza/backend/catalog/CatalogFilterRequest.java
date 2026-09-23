package com.andanza.backend.catalog;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

// Todos los campos son opcionales (el usuario puede aplicar 0 o varios
// filtros a la vez), por eso no llevan @NotBlank/@NotNull.
public record CatalogFilterRequest(
        String category,                // ej: "Deportivo", "Casual", "Formal", "Botas", "Sandalias"
        List<String> colors,            // ej: ["Negro", "Blanco"]
        List<String> sizes,             // ej: ["38", "40"]

        @PositiveOrZero(message = "El precio mínimo no puede ser negativo")
        BigDecimal minPrice,

        @DecimalMin(value = "0.0", inclusive = false, message = "El precio máximo debe ser mayor a 0")
        BigDecimal maxPrice,

        @Pattern(regexp = "price_asc|price_desc|newest", message = "Criterio de orden no soportado")
        String sort,                    // "price_asc" | "price_desc" | "newest"
        String search                   // texto libre (nombre o marca)
) {
}
