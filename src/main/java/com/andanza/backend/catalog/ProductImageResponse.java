package com.andanza.backend.catalog;

import java.util.UUID;

public record ProductImageResponse(
        UUID id,
        String color,
        String url,
        String thumbnailUrl,
        int sortOrder
) {

    public static ProductImageResponse from(ProductImage image) {
        return new ProductImageResponse(image.getId(), image.getColor(), image.getUrl(),
                image.getThumbnailUrl(), image.getSortOrder());
    }
}
