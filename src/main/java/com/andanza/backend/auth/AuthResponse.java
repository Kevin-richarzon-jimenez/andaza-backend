package com.andanza.backend.auth;

public record AuthResponse(
        String message,
        String email,
        String token
) {
}
