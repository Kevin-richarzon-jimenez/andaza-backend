package com.andanza.backend.comment;

import jakarta.validation.constraints.NotNull;

public record ModerateCommentRequest(
        @NotNull(message = "Debes indicar si apruebas o rechazas el comentario")
        Boolean approve
) {
}
