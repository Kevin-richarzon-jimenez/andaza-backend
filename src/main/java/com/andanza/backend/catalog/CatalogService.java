package com.andanza.backend.catalog;

import com.andanza.backend.exception.BusinessException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class CatalogService {
    private final List<Product> products = new CopyOnWriteArrayList<>(List.of(
            new Product("p1", "Runner Air", "Andanza", "Deportivo", new BigDecimal("289900"), List.of("Negro", "Blanco"), List.of("38", "40", "42")),
            new Product("p2", "Urban Loafer", "Andanza", "Casual", new BigDecimal("199900"), List.of("Café", "Negro"), List.of("39", "41", "43")),
            new Product("p3", "Oxford Classic", "Andanza", "Formal", new BigDecimal("259900"), List.of("Negro"), List.of("40", "42", "44")),
            new Product("p4", "Trail Boot", "Andanza", "Botas", new BigDecimal("329900"), List.of("Café", "Verde"), List.of("40", "41", "42")),
            new Product("p5", "Summer Slide", "Andanza", "Sandalias", new BigDecimal("129900"), List.of("Azul", "Blanco"), List.of("38", "40"))
    ));
    private final Set<String> categories = ConcurrentHashMap.newKeySet();

    public CatalogService() {
        categories.addAll(List.of("Deportivo", "Casual", "Formal", "Botas", "Sandalias"));
    }

    public Product findById(String productId) {
        return products.stream().filter(product -> product.id().equals(productId)).findFirst()
                .orElseThrow(() -> new BusinessException("productId", "El producto indicado no existe"));
    }

    public void validateVariant(Product product, String color, String size) {
        if (product.colors().stream().noneMatch(value -> value.equalsIgnoreCase(color))) {
            throw new BusinessException("color", "El color indicado no está disponible para este producto");
        }
        if (product.sizes().stream().noneMatch(value -> value.equalsIgnoreCase(size))) {
            throw new BusinessException("size", "La talla indicada no está disponible para este producto");
        }
    }

    public void addProduct(Product product) { products.add(product); }

    public boolean categoryExists(String category) {
        return categories.stream().anyMatch(value -> value.equalsIgnoreCase(category));
    }

    public void addCategory(String category) {
        if (categoryExists(category)) throw new BusinessException("name", "Ya existe una categoría con ese nombre");
        categories.add(category);
    }

    public void removeCategory(String category) { categories.removeIf(value -> value.equalsIgnoreCase(category)); }

    public List<ProductResponse> filter(CatalogFilterRequest filter) {
        if (filter.minPrice() != null && filter.maxPrice() != null
                && filter.minPrice().compareTo(filter.maxPrice()) > 0) {
            throw new BusinessException("minPrice", "El precio mínimo no puede ser mayor al precio máximo");
        }
        List<Product> result = products.stream()
                .filter(product -> filter.category() == null || product.category().equalsIgnoreCase(filter.category()))
                .filter(product -> filter.colors() == null || filter.colors().isEmpty()
                        || product.colors().stream().anyMatch(color -> containsIgnoreCase(filter.colors(), color)))
                .filter(product -> filter.sizes() == null || filter.sizes().isEmpty()
                        || product.sizes().stream().anyMatch(filter.sizes()::contains))
                .filter(product -> filter.minPrice() == null || product.price().compareTo(filter.minPrice()) >= 0)
                .filter(product -> filter.maxPrice() == null || product.price().compareTo(filter.maxPrice()) <= 0)
                .filter(product -> filter.search() == null || filter.search().isBlank()
                        || product.name().toLowerCase().contains(filter.search().toLowerCase())
                        || product.brand().toLowerCase().contains(filter.search().toLowerCase()))
                .collect(Collectors.toCollection(ArrayList::new));
        applySort(result, filter.sort());
        return result.stream().map(product -> new ProductResponse(product.id(), product.name(), product.brand(),
                product.category(), product.price(), product.colors(), product.sizes())).toList();
    }

    private boolean containsIgnoreCase(List<String> values, String value) {
        return values.stream().anyMatch(candidate -> candidate.equalsIgnoreCase(value));
    }

    private void applySort(List<Product> values, String sort) {
        if (sort == null) return;
        switch (sort) {
            case "price_asc" -> values.sort(Comparator.comparing(Product::price));
            case "price_desc" -> values.sort(Comparator.comparing(Product::price).reversed());
            case "newest" -> { }
            default -> throw new BusinessException("sort", "Criterio de orden no soportado: " + sort);
        }
    }
}
