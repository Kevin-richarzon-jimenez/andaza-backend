package com.andanza.backend.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {

    @Query("select distinct v.color from ProductVariant v order by v.color")
    List<String> findDistinctColors();

    @Query("select distinct v.size from ProductVariant v")
    List<String> findDistinctSizes();

    boolean existsByProductIdAndColorIgnoreCaseAndSize(UUID productId, String color, String size);
}
