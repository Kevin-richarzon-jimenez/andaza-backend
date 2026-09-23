package com.andanza.backend.comment;

import com.andanza.backend.catalog.CatalogService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

// Sin BD: cada comentario nuevo se simula con estado "pending" y un id
// generado; no queda almacenado. Este service lo usan dos controllers:
// CommentController (crear, lado cliente) y AdminCommentController
// (moderar, lado admin).
// TODO (BD): guardar el comentario asociado al usuario autenticado y al
// producto, con estado inicial "pending" para moderación.
@Service
public class CommentService {

    private final CatalogService catalogService;

    public CommentService(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    public CommentResponse create(CommentRequest request) {
        catalogService.findById(request.productId()); // lanza BusinessException si no existe

        return new CommentResponse(
                "c-" + UUID.randomUUID().toString().substring(0, 8),
                request.productId(),
                request.rating(),
                request.text(),
                Instant.now(),
                "pending"
        );
    }

    public String moderate(String commentId, boolean approve) {
        // TODO (BD): actualizar el estado real del comentario (approved/rejected).
        return approve
                ? "Comentario " + commentId + " aprobado y publicado"
                : "Comentario " + commentId + " rechazado";
    }
}
