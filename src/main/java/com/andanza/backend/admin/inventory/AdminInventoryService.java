package com.andanza.backend.admin.inventory;

import com.andanza.backend.catalog.CatalogService;
import com.andanza.backend.catalog.Product;
import org.springframework.stereotype.Service;

// Sin BD: valida que el producto exista de verdad (contra el catálogo),
// que la combinación color/talla sea una variante real de ese producto,
// y confirma la operación; no actualiza ningún stock real.
// TODO (BD): actualizar el stock real en la tabla "inventory"
// (product_id, color, size, stock).
@Service
public class AdminInventoryService {

    private final CatalogService catalogService;

    public AdminInventoryService(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    public String updateStock(AdminInventoryUpdateRequest request) {
        Product product = catalogService.findById(request.productId());
        catalogService.validateVariant(product, request.color(), request.size());

        return "Stock actualizado a " + request.stock() + " unidades para "
                + request.productId() + " (" + request.color() + ", talla " + request.size() + ")";
    }
}
