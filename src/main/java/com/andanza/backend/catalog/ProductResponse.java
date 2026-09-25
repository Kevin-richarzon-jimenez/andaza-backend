package com.andanza.backend.catalog;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String brand,
        String description,
        CategoryResponse category,
        BigDecimal price,
        List<ProductVariantResponse> variants,
        List<ProductImageResponse> images
) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getBrand(),
                product.getDescription(),
                CategoryResponse.from(product.getCategory()),
                product.getPrice(),
                product.getVariants().stream().map(ProductVariantResponse::from).toList(),
                product.getImages().stream().map(ProductImageResponse::from).toList());
    }
}
