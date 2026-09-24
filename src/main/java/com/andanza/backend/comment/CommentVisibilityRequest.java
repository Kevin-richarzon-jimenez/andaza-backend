package com.andanza.backend.comment;

import jakarta.validation.constraints.NotNull;

public record CommentVisibilityRequest(
        @NotNull(message = "Debes indicar si el comentario debe verse o quedar oculto")
        Boolean visible
) {
}
