package com.andanza.backend.catalog;

import java.math.BigDecimal;
import java.util.List;

// Lo que existe hoy en el catálogo, para que el frontend arme los filtros sin tener las opciones escritas a mano.
// Los precios son null si todavía no hay productos.
public record CatalogFilterOptionsResponse(
        List<String> colors,
        List<String> sizes,
        BigDecimal minPrice,
        BigDecimal maxPrice
) {
}
