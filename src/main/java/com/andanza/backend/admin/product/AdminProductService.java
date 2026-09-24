package com.andanza.backend.admin.product;

import com.andanza.backend.catalog.Category;
import com.andanza.backend.catalog.CategoryRepository;
import com.andanza.backend.catalog.Product;
import com.andanza.backend.catalog.ProductRepository;
import com.andanza.backend.catalog.ProductResponse;
import com.andanza.backend.catalog.ProductVariant;
import com.andanza.backend.catalog.ProductVariantRepository;
import com.andanza.backend.catalog.ProductVariantResponse;
import com.andanza.backend.exception.BusinessException;
import com.andanza.backend.exception.ConflictException;
import com.andanza.backend.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class AdminProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantRepository variantRepository;

    public AdminProductService(ProductRepository productRepository, CategoryRepository categoryRepository,
                               ProductVariantRepository variantRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.variantRepository = variantRepository;
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

    @Transactional
    public ProductResponse update(UUID id, AdminProductUpdateRequest request) {
        Product product = findProduct(id);
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new BusinessException("categoryId", "La categoría indicada no existe"));
        product.setName(request.name().trim());
        product.setBrand(request.brand().trim());
        product.setDescription(request.description().trim());
        product.setCategory(category);
        product.setPrice(request.price());
        return ProductResponse.from(product);
    }

    // Borra el producto con sus variantes; los favoritos y comentarios se borran con él (ON DELETE CASCADE).
    @Transactional
    public void delete(UUID id) {
        productRepository.delete(findProduct(id));
    }

    @Transactional
    public ProductVariantResponse addVariant(UUID productId, ProductVariantRequest request) {
        Product product = findProduct(productId);
        String color = request.color().trim();
        String size = request.size().trim();
        if (variantRepository.existsByProductIdAndColorIgnoreCaseAndSize(productId, color, size)) {
            throw new ConflictException("variants", "La variante " + color + " talla " + size + " ya existe en este producto");
        }
        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setColor(color);
        variant.setSize(size);
        variant.setStock(request.stock());
        return ProductVariantResponse.from(variantRepository.save(variant));
    }

    private Product findProduct(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("id", "El producto indicado no existe"));
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
