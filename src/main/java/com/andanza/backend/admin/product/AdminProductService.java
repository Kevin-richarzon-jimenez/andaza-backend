package com.andanza.backend.admin.product;

import com.andanza.backend.catalog.Category;
import com.andanza.backend.catalog.CategoryRepository;
import com.andanza.backend.catalog.Product;
import com.andanza.backend.catalog.ProductRepository;
import com.andanza.backend.catalog.ProductResponse;
import com.andanza.backend.catalog.ProductVariant;
import com.andanza.backend.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class AdminProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public AdminProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public ProductResponse create(AdminProductRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new BusinessException("categoryId", "La categoría indicada no existe"));
        validateNoDuplicateVariants(request.variants());

        Product product = new Product();
        product.setName(request.name().trim());
        product.setBrand(request.brand().trim());
        product.setDescription(request.description().trim());
        product.setCategory(category);
        product.setPrice(request.price());
        for (ProductVariantRequest variantRequest : request.variants()) {
            ProductVariant variant = new ProductVariant();
            variant.setColor(variantRequest.color().trim());
            variant.setSize(variantRequest.size().trim());
            variant.setStock(variantRequest.stock());
            product.addVariant(variant);
        }
        return ProductResponse.from(productRepository.save(product));
    }

    private void validateNoDuplicateVariants(List<ProductVariantRequest> variants) {
        Set<String> seen = new HashSet<>();
        for (ProductVariantRequest variant : variants) {
            String key = variant.color().trim().toLowerCase(Locale.ROOT) + "|" + variant.size().trim();
            if (!seen.add(key)) {
                throw new BusinessException("variants",
                        "La variante " + variant.color().trim() + " talla " + variant.size().trim() + " está repetida");
            }
        }
    }
}
