package com.andanza.backend.catalog;

import java.math.BigDecimal;
import java.util.List;

// Modelo plano en memoria -- representa la futura entidad JPA "Product"
// que se creará cuando se integre la persistencia real del catálogo.
public record Product(
        String id,
        String name,
        String brand,
        String category,
        BigDecimal price,
        List<String> colors,
        List<String> sizes
) {
}
