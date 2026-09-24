package com.andanza.backend.common;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

// Paginación por query params: page (base 0) y size, ambos opcionales.
public record PageParams(
        @Min(value = 0, message = "La página no puede ser negativa")
        Integer page,

        @Min(value = 1, message = "El tamaño de página mínimo es 1")
        @Max(value = 100, message = "El tamaño de página máximo es 100")
        Integer size
) {

    private static final int DEFAULT_SIZE = 20;

    public Pageable toPageable(Sort sort) {
        return PageRequest.of(page != null ? page : 0, size != null ? size : DEFAULT_SIZE, sort);
    }
}
