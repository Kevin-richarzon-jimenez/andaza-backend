package com.andanza.backend.catalog;

import com.andanza.backend.exception.BusinessException;
import com.andanza.backend.exception.NotFoundException;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class CatalogService {

    private static final int DEFAULT_PAGE_SIZE = 20;

    // Las tallas numéricas se ordenan como números ("9" antes que "38", "37.5" entre "37" y "38"); las demás, al final.
    private static final Comparator<String> SIZE_ORDER = Comparator
            .comparing(CatalogService::sizeAsNumber, Comparator.nullsLast(Comparator.<BigDecimal>naturalOrder()))
            .thenComparing(Comparator.naturalOrder());

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantRepository variantRepository;

    public CatalogService(ProductRepository productRepository, CategoryRepository categoryRepository,
                          ProductVariantRepository variantRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.variantRepository = variantRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> search(CatalogFilterRequest filter) {
        if (filter.minPrice() != null && filter.maxPrice() != null
                && filter.minPrice().compareTo(filter.maxPrice()) > 0) {
            throw new BusinessException("minPrice", "El precio mínimo no puede ser mayor al precio máximo");
        }
        return PageResponse.from(productRepository.findAll(toSpecification(filter), toPageable(filter))
                .map(ProductResponse::from));
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(UUID id) {
        return productRepository.findById(id)
                .map(ProductResponse::from)
                .orElseThrow(() -> new NotFoundException("id", "El producto indicado no existe"));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listCategories() {
        return categoryRepository.findAllByOrderByNameAsc().stream().map(CategoryResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public CatalogFilterOptionsResponse getFilterOptions() {
        List<String> sizes = variantRepository.findDistinctSizes().stream().sorted(SIZE_ORDER).toList();
        return new CatalogFilterOptionsResponse(
                variantRepository.findDistinctColors(),
                sizes,
                productRepository.findMinPrice(),
                productRepository.findMaxPrice());
    }

    private static BigDecimal sizeAsNumber(String size) {
        try {
            return new BigDecimal(size.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Pageable toPageable(CatalogFilterRequest filter) {
        int page = filter.page() != null ? filter.page() : 0;
        int size = filter.size() != null ? filter.size() : DEFAULT_PAGE_SIZE;
        Sort sort = switch (filter.sort() == null ? "" : filter.sort()) {
            case "price_asc" -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            case "newest" -> Sort.by("createdAt").descending();
            default -> Sort.by("name").ascending();
        };
        return PageRequest.of(page, size, sort.and(Sort.by("id")));
    }

    private Specification<Product> toSpecification(CatalogFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (hasText(filter.category())) {
                predicates.add(cb.equal(cb.lower(root.get("category").get("name")), lower(filter.category())));
            }
            if (filter.minPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filter.minPrice()));
            }
            if (filter.maxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.maxPrice()));
            }
            if (hasText(filter.search())) {
                String pattern = "%" + lower(filter.search()) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("brand")), pattern)));
            }
            boolean byColor = filter.colors() != null && !filter.colors().isEmpty();
            boolean bySize = filter.sizes() != null && !filter.sizes().isEmpty();
            if (byColor || bySize) {
                // Un solo join: color y talla se exigen sobre la misma variante.
                Join<Product, ProductVariant> variant = root.join("variants");
                if (byColor) {
                    predicates.add(cb.lower(variant.get("color")).in(filter.colors().stream().map(this::lower).toList()));
                }
                if (bySize) {
                    predicates.add(variant.get("size").in(filter.sizes()));
                }
                query.distinct(true);
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String lower(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
