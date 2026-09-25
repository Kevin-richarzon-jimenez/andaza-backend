package com.andanza.backend.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductImageRepository extends JpaRepository<ProductImage, UUID> {

    Optional<ProductImage> findByIdAndProductId(UUID id, UUID productId);

    List<ProductImage> findByProductIdAndColorIgnoreCaseOrderBySortOrderAscIdAsc(UUID productId, String color);
}
