package com.andanza.backend.comment;

import java.time.Instant;

public record CommentResponse(
        String id,
        String productId,
        int rating,
        String text,
        Instant createdAt,
        String status // "pending" hasta moderación (ver Admin — Comentarios)
) {
}
