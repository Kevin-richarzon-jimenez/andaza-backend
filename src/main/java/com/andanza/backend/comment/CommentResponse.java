package com.andanza.backend.comment;

import java.time.Instant;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        UUID productId,
        String productName,
        String authorName,
        int rating,
        String text,
        CommentStatus status,
        Instant createdAt
) {

    // El autor se muestra como "Nombre A." para no exponer el apellido completo.
    public static CommentResponse from(Comment comment) {
        String lastName = comment.getUser().getLastName();
        String authorName = comment.getUser().getFirstName() + " " + lastName.charAt(0) + ".";
        return new CommentResponse(comment.getId(), comment.getProduct().getId(), comment.getProduct().getName(),
                authorName, comment.getRating(), comment.getText(), comment.getStatus(), comment.getCreatedAt());
    }
}
