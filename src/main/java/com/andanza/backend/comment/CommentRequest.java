package com.andanza.backend.comment;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CommentRequest(
        @NotNull(message = "El producto es obligatorio")
        UUID productId,

        @NotNull(message = "La calificación es obligatoria")
        @Min(value = 1, message = "La calificación mínima es 1 estrella")
        @Max(value = 5, message = "La calificación máxima es 5 estrellas")
        Integer rating,

        @NotBlank(message = "El comentario no puede estar vacío")
        @Size(min = 5, max = 500, message = "El comentario debe tener entre 5 y 500 caracteres")
        String text
) {
}
