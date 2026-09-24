package com.andanza.backend.admin.inventory;

import com.andanza.backend.catalog.ProductVariantResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/inventory")
@Tag(name = "Admin - Inventory")
public class AdminInventoryController {

    private final AdminInventoryService adminInventoryService;

    public AdminInventoryController(AdminInventoryService adminInventoryService) {
        this.adminInventoryService = adminInventoryService;
    }

    // El inventario se identifica por la variante (producto + color + talla) a la que pertenece el stock.
    @PutMapping("/{variantId}")
    public ResponseEntity<ProductVariantResponse> updateStock(
            @PathVariable UUID variantId, @Valid @RequestBody AdminInventoryUpdateRequest request) {
        return ResponseEntity.ok(adminInventoryService.updateStock(variantId, request));
    }
}
