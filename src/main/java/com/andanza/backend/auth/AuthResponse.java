package com.andanza.backend.auth;

import com.andanza.backend.user.UserResponse;

public record AuthResponse(
        String message,
        String token,
        UserResponse user
) {
}
