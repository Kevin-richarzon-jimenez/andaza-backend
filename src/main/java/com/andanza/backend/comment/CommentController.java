package com.andanza.backend.comment;

import com.andanza.backend.auth.CurrentUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/comments")
    public ResponseEntity<CommentResponse> create(@AuthenticationPrincipal Jwt jwt,
                                                  @Valid @RequestBody CommentRequest request) {
        return ResponseEntity.status(201).body(commentService.create(CurrentUser.id(jwt), request));
    }

    // Público: solo los comentarios publicados (no ocultos) de un producto.
    @GetMapping("/comments")
    public ResponseEntity<List<CommentResponse>> listByProduct(@RequestParam UUID productId) {
        return ResponseEntity.ok(commentService.listPublished(productId));
    }

    // Los comentarios del usuario autenticado, en cualquier estado.
    @GetMapping("/account/comments")
    public ResponseEntity<List<CommentResponse>> listMine(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(commentService.listMine(CurrentUser.id(jwt)));
    }
}
