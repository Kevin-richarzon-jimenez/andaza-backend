package com.andanza.backend.admin.product;

import com.andanza.backend.catalog.CatalogService;
import com.andanza.backend.catalog.Product;
import com.andanza.backend.catalog.ProductResponse;
import com.andanza.backend.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminProductService {

    private final CatalogService catalogService;

    public AdminProductService(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    public ProductResponse create(AdminProductRequest request) {
        if (!catalogService.categoryExists(request.category())) {
            throw new BusinessException("category", "Categoría no reconocida: " + request.category());
        }

        validateNoDuplicateColors(request.variants());

        List<String> colors = request.variants().stream().map(ColorVariantRequest::color).toList();
        List<String> sizes = request.variants().stream()
                .flatMap(v -> v.sizes().stream())
                .distinct()
                .toList();

        // TODO (BD): persistir el producto y sus variantes (con imagen y stock inicial por talla).
        String generatedId = "p-" + UUID.randomUUID().toString().substring(0, 8);
        Product product = new Product(generatedId, request.name(), request.brand(),
                request.category(), request.price(), colors, sizes);

        // El producto pasa a formar parte del mismo catálogo que ve el
        // cliente -- así se puede probar de punta a punta (crear producto
        // en Admin -> verlo/agregarlo al carrito en el catálogo público).
        catalogService.addProduct(product);

        return new ProductResponse(product.id(), product.name(), product.brand(),
                product.category(), product.price(), product.colors(), product.sizes());
    }

    private void validateNoDuplicateColors(List<ColorVariantRequest> variants) {
        long uniqueColors = variants.stream()
                .map(v -> v.color().toLowerCase())
                .collect(Collectors.toSet())
                .size();
        if (uniqueColors != variants.size()) {
            throw new BusinessException("variants", "No puedes repetir el mismo color en dos variantes");
        }
    }
}
