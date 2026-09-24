package com.andanza.backend.catalog;

import java.util.UUID;

public record ProductVariantResponse(
        UUID id,
        String color,
        String size,
        int stock
) {

    public static ProductVariantResponse from(ProductVariant variant) {
        return new ProductVariantResponse(variant.getId(), variant.getColor(), variant.getSize(), variant.getStock());
    }
}
