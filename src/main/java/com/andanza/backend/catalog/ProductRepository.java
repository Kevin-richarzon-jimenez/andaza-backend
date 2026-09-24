package com.andanza.backend.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    long countByCategoryId(UUID categoryId);

    @Query("select min(p.price) from Product p")
    BigDecimal findMinPrice();

    @Query("select max(p.price) from Product p")
    BigDecimal findMaxPrice();
}
