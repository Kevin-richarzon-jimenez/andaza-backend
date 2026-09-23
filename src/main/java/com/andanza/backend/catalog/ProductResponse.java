package com.andanza.backend.catalog;

import java.math.BigDecimal;
import java.util.List;

public record ProductResponse(
        String id,
        String name,
        String brand,
        String category,
        BigDecimal price,
        List<String> colors,
        List<String> sizes
) {
}
