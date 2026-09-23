package com.andanza.backend.catalog;

import com.andanza.backend.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

// TODO (BD): reemplazar todo esto por consultas reales (repositorio JPA)
// cuando existan las tablas "product" y "category". Mientras tanto, esto
// es la "base de datos" compartida en memoria del demo: mutable a propósito,
// porque Admin - Categorías y Admin - Productos escriben acá para que un
// producto/categoría creado por Admin se pueda ver de verdad desde el
// catálogo público y desde Carrito/Favoritos/Comentarios/Inventario.
@Service
public class CatalogService {

    // CopyOnWriteArrayList porque se lee muchísimo más de lo que se escribe
    // (cada visita al catálogo lee, un alta de Admin escribe de vez en cuando).
    private final List<Product> products = new CopyOnWriteArrayList<>(List.of(
            new Product("p1", "Runner Air", "Andanza", "Deportivo",
                    new BigDecimal("289900"), List.of("Negro", "Blanco"), List.of("38", "40", "42")),
            new Product("p2", "Urban Loafer", "Andanza", "Casual",
                    new BigDecimal("199900"), List.of("Café", "Negro"), List.of("39", "41", "43")),
            new Product("p3", "Oxford Classic", "Andanza", "Formal",
                    new BigDecimal("259900"), List.of("Negro"), List.of("40", "42", "44")),
            new Product("p4", "Trail Boot", "Andanza", "Botas",
                    new BigDecimal("329900"), List.of("Café", "Verde"), List.of("40", "41", "42")),
            new Product("p5", "Summer Slide", "Andanza", "Sandalias",
                    new BigDecimal("129900"), List.of("Azul", "Blanco"), List.of("38", "40"))
    ));

    private final Set<String> categories = ConcurrentHashMap.newKeySet();

    public CatalogService() {
        categories.addAll(List.of("Deportivo", "Casual", "Formal", "Botas", "Sandalias"));
    }

    // Usado por Carrito, Favoritos, Comentarios y Admin - Inventario para
    // validar que un producto existe y para obtener su precio real (nunca
    // confiar en el precio que mande el cliente).
    public Product findById(String productId) {
        return products.stream()
                .filter(p -> p.id().equals(productId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("productId", "El producto indicado no existe"));
    }

    // Usado por Carrito y Admin - Inventario para confirmar que la talla y
    // el color pedidos son de verdad opciones de ese producto -- igual que
    // con el precio, no basta con que el producto exista, la variante
    // también tiene que ser real.
    public void validateVariant(Product product, String color, String size) {
        boolean validColor = product.colors().stream().anyMatch(c -> c.equalsIgnoreCase(color));
        if (!validColor) {
            throw new BusinessException("color", "El color indicado no está disponible para este producto");
        }
        boolean validSize = product.sizes().stream().anyMatch(s -> s.equalsIgnoreCase(size));
        if (!validSize) {
            throw new BusinessException("size", "La talla indicada no está disponible para este producto");
        }
    }

    // Usado por Admin - Productos: un producto creado por Admin pasa a
    // formar parte del mismo catálogo que ve el cliente.
    public void addProduct(Product product) {
        products.add(product);
    }

    // Usado por Admin - Categorías y Admin - Productos, para que ambos
    // dominios compartan la misma lista real de categorías válidas en vez
    // de cada uno tener la suya por separado.
    public boolean categoryExists(String category) {
        return categories.stream().anyMatch(c -> c.equalsIgnoreCase(category));
    }

    public void addCategory(String category) {
        if (categoryExists(category)) {
            throw new BusinessException("name", "Ya existe una categoría con ese nombre");
        }
        categories.add(category);
    }

    public void removeCategory(String category) {
        categories.removeIf(c -> c.equalsIgnoreCase(category));
    }

    public List<ProductResponse> filter(CatalogFilterRequest filter) {
        if (filter.minPrice() != null && filter.maxPrice() != null
                && filter.minPrice().compareTo(filter.maxPrice()) > 0) {
            throw new BusinessException("minPrice", "El precio mínimo no puede ser mayor al precio máximo");
        }

        List<Product> result = products.stream()
                .filter(p -> filter.category() == null
                        || p.category().equalsIgnoreCase(filter.category()))
                .filter(p -> filter.colors() == null || filter.colors().isEmpty()
                        || p.colors().stream().anyMatch(c -> containsIgnoreCase(filter.colors(), c)))
                .filter(p -> filter.sizes() == null || filter.sizes().isEmpty()
                        || p.sizes().stream().anyMatch(s -> filter.sizes().contains(s)))
                .filter(p -> filter.minPrice() == null || p.price().compareTo(filter.minPrice()) >= 0)
                .filter(p -> filter.maxPrice() == null || p.price().compareTo(filter.maxPrice()) <= 0)
                .filter(p -> filter.search() == null || filter.search().isBlank()
                        || p.name().toLowerCase().contains(filter.search().toLowerCase())
                        || p.brand().toLowerCase().contains(filter.search().toLowerCase()))
                .collect(Collectors.toCollection(ArrayList::new));

        applySort(result, filter.sort());

        return result.stream()
                .map(p -> new ProductResponse(p.id(), p.name(), p.brand(), p.category(),
                        p.price(), p.colors(), p.sizes()))
                .toList();
    }

    private boolean containsIgnoreCase(List<String> list, String value) {
        return list.stream().anyMatch(v -> v.equalsIgnoreCase(value));
    }

    private void applySort(List<Product> products, String sort) {
        if (sort == null) return;
        switch (sort) {
            case "price_asc" -> products.sort(Comparator.comparing(Product::price));
            case "price_desc" -> products.sort(Comparator.comparing(Product::price).reversed());
            case "newest" -> { /* TODO (BD): ordenar por fecha de creación real */ }
            default -> throw new BusinessException("sort", "Criterio de orden no soportado: " + sort);
        }
    }
}
