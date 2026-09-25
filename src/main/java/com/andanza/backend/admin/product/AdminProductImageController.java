package com.andanza.backend.admin.product;

import com.andanza.backend.catalog.ProductImageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/products/{productId}/images")
@Tag(name = "Admin - Product images")
public class AdminProductImageController {

    private final ProductImageService imageService;

    public AdminProductImageController(ProductImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductImageResponse> add(@PathVariable UUID productId,
                                                    @RequestParam String color,
                                                    @RequestPart("image") MultipartFile image,
                                                    @RequestPart("thumbnail") MultipartFile thumbnail) {
        return ResponseEntity.status(201).body(imageService.add(productId, color, image, thumbnail));
    }

    @PutMapping("/{imageId}/cover")
    public ResponseEntity<List<ProductImageResponse>> makeCover(@PathVariable UUID productId,
                                                                @PathVariable UUID imageId) {
        return ResponseEntity.ok(imageService.makeCover(productId, imageId));
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> remove(@PathVariable UUID productId, @PathVariable UUID imageId) {
        imageService.remove(productId, imageId);
        return ResponseEntity.noContent().build();
    }
}
