package com.andanza.backend.config;

import com.andanza.backend.user.AccountStatus;
import com.andanza.backend.user.Role;
import com.andanza.backend.user.User;
import com.andanza.backend.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserJwtAuthenticationConverterTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserJwtAuthenticationConverter converter;

    private Jwt tokenOf(String subject) {
        return Jwt.withTokenValue("token").header("alg", "HS256").subject(subject)
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build();
    }

    private User user(UUID id, Role role, AccountStatus status) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        user.setAccountStatus(status);
        return user;
    }

    @Test
    void takesTheRoleFromTheDatabaseAndNotFromTheToken() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.of(user(id, Role.ADMIN, AccountStatus.ACTIVE)));

        var authentication = converter.convert(tokenOf(id.toString()));

        assertThat(authentication.getAuthorities()).containsExactly(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @Test
    void aDemotedAdministratorLosesTheRoleImmediately() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.of(user(id, Role.CUSTOMER, AccountStatus.ACTIVE)));

        var authentication = converter.convert(tokenOf(id.toString()));

        assertThat(authentication.getAuthorities()).containsExactly(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
    }

    @Test
    void rejectsTheTokenOfABlockedUser() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.of(user(id, Role.CUSTOMER, AccountStatus.BLOCKED)));

        assertThatThrownBy(() -> converter.convert(tokenOf(id.toString())))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void rejectsTheTokenOfAUserThatNoLongerExists() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> converter.convert(tokenOf(id.toString())))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void rejectsATokenWhoseSubjectIsNotAUserId() {
        assertThatThrownBy(() -> converter.convert(tokenOf("no-es-un-uuid")))
                .isInstanceOf(BadCredentialsException.class);
    }
}
