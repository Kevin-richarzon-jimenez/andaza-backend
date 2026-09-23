package com.andanza.backend.admin.inventory;

import com.andanza.backend.common.MessageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/inventory")
@Tag(name = "Admin - Inventory")
public class AdminInventoryController {

    private final AdminInventoryService adminInventoryService;

    public AdminInventoryController(AdminInventoryService adminInventoryService) {
        this.adminInventoryService = adminInventoryService;
    }

    @PutMapping
    public ResponseEntity<MessageResponse> updateStock(@Valid @RequestBody AdminInventoryUpdateRequest request) {
        return ResponseEntity.ok(new MessageResponse(adminInventoryService.updateStock(request)));
    }
}
