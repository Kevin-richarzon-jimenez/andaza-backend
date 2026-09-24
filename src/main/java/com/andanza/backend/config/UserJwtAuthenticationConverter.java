package com.andanza.backend.config;

import com.andanza.backend.user.AccountStatus;
import com.andanza.backend.user.User;
import com.andanza.backend.user.UserRepository;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

// El token solo prueba quién es el usuario (claim "sub"). El rol y el estado de la cuenta se leen de la base
// en cada petición, así que bloquear a un usuario o quitarle el rol de administrador surte efecto al instante,
// sin esperar a que su token expire.
@Component
public class UserJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserRepository userRepository;

    public UserJwtAuthenticationConverter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        UUID userId;
        try {
            userId = UUID.fromString(jwt.getSubject());
        } catch (RuntimeException e) {
            throw new BadCredentialsException("Token inválido");
        }
        User user = userRepository.findById(userId)
                .filter(found -> found.getAccountStatus() == AccountStatus.ACTIVE)
                .orElseThrow(() -> new BadCredentialsException("La sesión ya no es válida"));
        return new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
    }
}
