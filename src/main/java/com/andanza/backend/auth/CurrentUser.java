package com.andanza.backend.auth;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

// El id del usuario autenticado sale del token (claim "sub"); nunca viaja en el body ni en la URL.
public final class CurrentUser {

    private CurrentUser() {
    }

    public static UUID id(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
