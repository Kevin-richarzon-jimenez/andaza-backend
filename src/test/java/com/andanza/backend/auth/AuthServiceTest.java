package com.andanza.backend.auth;

import com.andanza.backend.exception.ConflictException;
import com.andanza.backend.exception.ForbiddenException;
import com.andanza.backend.exception.TooManyRequestsException;
import com.andanza.backend.exception.UnauthorizedException;
import com.andanza.backend.user.AccountStatus;
import com.andanza.backend.user.User;
import com.andanza.backend.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String IP = "127.0.0.1";

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtService, new LoginAttemptLimiter());
        lenient().when(jwtService.generateToken(any())).thenReturn("token");
    }

    private RegisterRequest registration(String email) {
        return new RegisterRequest("Ana", "Lopez", email, "Segura123", "Segura123");
    }

    private User customer(String rawPassword, AccountStatus status) {
        User user = new User();
        user.setEmail("ana@example.com");
        user.setFirstName("Ana");
        user.setLastName("Lopez");
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setAccountStatus(status);
        user.setId(UUID.randomUUID());
        return user;
    }

    private void anaExists(AccountStatus status) {
        when(userRepository.findByEmailIgnoreCase("ana@example.com"))
                .thenReturn(Optional.of(customer("Segura123", status)));
    }

    @Test
    void registerStoresTheEmailInLowercaseAndNeverTheRawPassword() {
        when(userRepository.existsByEmailIgnoreCase("ana@example.com")).thenReturn(false);

        authService.register(registration("Ana@Example.com"));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getEmail()).isEqualTo("ana@example.com");
        assertThat(saved.getValue().getPasswordHash()).isNotEqualTo("Segura123");
        assertThat(passwordEncoder.matches("Segura123", saved.getValue().getPasswordHash())).isTrue();
    }

    @Test
    void registerRejectsAnEmailThatAlreadyExists() {
        when(userRepository.existsByEmailIgnoreCase("ana@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registration("ana@example.com")))
                .isInstanceOf(ConflictException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void loginGivesTheSameErrorForAWrongPasswordAndAnUnknownEmail() {
        anaExists(AccountStatus.ACTIVE);
        when(userRepository.findByEmailIgnoreCase("nadie@example.com")).thenReturn(Optional.empty());

        Throwable wrongPassword = catchThrowable(() -> authService.login(new LoginRequest("ana@example.com", "Otra12345"), IP));
        Throwable unknownEmail = catchThrowable(() -> authService.login(new LoginRequest("nadie@example.com", "Segura123"), IP));

        assertThat(wrongPassword).isInstanceOf(UnauthorizedException.class);
        assertThat(unknownEmail).isInstanceOf(UnauthorizedException.class);
        assertThat(wrongPassword.getMessage()).isEqualTo(unknownEmail.getMessage());
    }

    @Test
    void loginRejectsABlockedAccountEvenWithTheRightPassword() {
        anaExists(AccountStatus.BLOCKED);

        assertThatThrownBy(() -> authService.login(new LoginRequest("ana@example.com", "Segura123"), IP))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void loginBlocksTheAddressAfterFiveFailuresEvenWithTheRightPassword() {
        anaExists(AccountStatus.ACTIVE);
        for (int i = 0; i < 5; i++) {
            assertThatThrownBy(() -> authService.login(new LoginRequest("ana@example.com", "Otra12345"), IP))
                    .isInstanceOf(UnauthorizedException.class);
        }

        assertThatThrownBy(() -> authService.login(new LoginRequest("ana@example.com", "Segura123"), IP))
                .isInstanceOf(TooManyRequestsException.class);
    }

    @Test
    void theBlockOnOneAddressDoesNotAffectAnother() {
        anaExists(AccountStatus.ACTIVE);
        for (int i = 0; i < 5; i++) {
            assertThatThrownBy(() -> authService.login(new LoginRequest("ana@example.com", "Otra12345"), IP))
                    .isInstanceOf(UnauthorizedException.class);
        }

        assertThat(authService.login(new LoginRequest("ana@example.com", "Segura123"), "10.0.0.9").token())
                .isEqualTo("token");
    }

    @Test
    void aSuccessfulLoginResetsTheFailureCount() {
        anaExists(AccountStatus.ACTIVE);
        for (int i = 0; i < 4; i++) {
            assertThatThrownBy(() -> authService.login(new LoginRequest("ana@example.com", "Otra12345"), IP))
                    .isInstanceOf(UnauthorizedException.class);
        }
        authService.login(new LoginRequest("ana@example.com", "Segura123"), IP);

        for (int i = 0; i < 4; i++) {
            assertThatThrownBy(() -> authService.login(new LoginRequest("ana@example.com", "Otra12345"), IP))
                    .isInstanceOf(UnauthorizedException.class);
        }
    }
}
