package com.andanza.backend.admin.inventory;

import com.andanza.backend.catalog.ProductVariant;
import com.andanza.backend.catalog.ProductVariantRepository;
import com.andanza.backend.catalog.ProductVariantResponse;
import com.andanza.backend.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AdminInventoryService {

    private final ProductVariantRepository variantRepository;

    public AdminInventoryService(ProductVariantRepository variantRepository) {
        this.variantRepository = variantRepository;
    }

    @Transactional
    public ProductVariantResponse updateStock(UUID variantId, AdminInventoryUpdateRequest request) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new NotFoundException("variantId", "La variante indicada no existe"));
        variant.setStock(request.stock());
        return ProductVariantResponse.from(variant);
    }
}
