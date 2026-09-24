package com.andanza.backend.favorite;

import com.andanza.backend.auth.CurrentUser;
import com.andanza.backend.catalog.ProductResponse;
import com.andanza.backend.common.MessageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/favorites")
@Tag(name = "Favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> list(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(favoriteService.list(CurrentUser.id(jwt)));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<MessageResponse> add(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID productId) {
        favoriteService.add(CurrentUser.id(jwt), productId);
        return ResponseEntity.ok(new MessageResponse("Producto agregado a favoritos"));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> remove(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID productId) {
        favoriteService.remove(CurrentUser.id(jwt), productId);
        return ResponseEntity.noContent().build();
    }
}
