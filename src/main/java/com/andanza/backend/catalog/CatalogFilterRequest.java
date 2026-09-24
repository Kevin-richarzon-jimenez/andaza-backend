package com.andanza.backend.catalog;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

// Todos los campos son opcionales (el usuario puede aplicar 0 o varios filtros a la vez),
// por eso no llevan @NotBlank/@NotNull. Si se piden color y talla a la vez, deben existir en la misma variante.
public record CatalogFilterRequest(
        String category,                // nombre de la categoría, ej: "Deportivo"
        List<String> colors,            // ej: ["Negro", "Blanco"]
        List<String> sizes,             // ej: ["38", "40"]

        @PositiveOrZero(message = "El precio mínimo no puede ser negativo")
        BigDecimal minPrice,

        @DecimalMin(value = "0.0", inclusive = false, message = "El precio máximo debe ser mayor a 0")
        BigDecimal maxPrice,

        @Pattern(regexp = "price_asc|price_desc|newest", message = "Criterio de orden no soportado")
        String sort,                    // "price_asc" | "price_desc" | "newest"; por defecto, por nombre
        String search,                  // texto libre (nombre o marca)

        @Min(value = 0, message = "La página no puede ser negativa")
        Integer page,                   // base 0; por defecto 0

        @Min(value = 1, message = "El tamaño de página mínimo es 1")
        @Max(value = 100, message = "El tamaño de página máximo es 100")
        Integer size                    // por defecto 20
) {
}
