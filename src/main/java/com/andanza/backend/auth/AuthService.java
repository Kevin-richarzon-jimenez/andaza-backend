package com.andanza.backend.auth;

import com.andanza.backend.exception.BusinessException;
import com.andanza.backend.exception.ConflictException;
import com.andanza.backend.exception.ForbiddenException;
import com.andanza.backend.exception.NotFoundException;
import com.andanza.backend.exception.UnauthorizedException;
import com.andanza.backend.user.AccountStatus;
import com.andanza.backend.user.User;
import com.andanza.backend.user.UserRepository;
import com.andanza.backend.user.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginAttemptLimiter loginAttemptLimiter;
    private final String dummyHash;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
                       LoginAttemptLimiter loginAttemptLimiter) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.loginAttemptLimiter = loginAttemptLimiter;
        this.dummyHash = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("email", "Ya existe una cuenta registrada con este correo");
        }
        User user = new User();
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        userRepository.save(user);
        return new AuthResponse("Cuenta creada correctamente", jwtService.generateToken(user), UserResponse.from(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request, String clientIp) {
        String email = request.email().trim().toLowerCase();
        String attemptKey = email + "|" + clientIp;
        loginAttemptLimiter.checkAllowed(attemptKey);

        // Si el correo no existe se compara contra un hash falso: así el login tarda lo mismo y el tiempo
        // de respuesta no revela cuáles correos están registrados. El mensaje también es el mismo.
        Optional<User> found = userRepository.findByEmailIgnoreCase(email);
        String hash = found.map(User::getPasswordHash).orElse(dummyHash);
        boolean valid = passwordEncoder.matches(request.password(), hash) && found.isPresent();
        if (!valid) {
            loginAttemptLimiter.recordFailure(attemptKey);
            throw new UnauthorizedException("password", "Correo o contraseña incorrectos");
        }

        User user = found.get();
        loginAttemptLimiter.reset(attemptKey);
        if (user.getAccountStatus() == AccountStatus.BLOCKED) {
            throw new ForbiddenException("email", "Tu cuenta está bloqueada. Contacta a soporte.");
        }
        return new AuthResponse("Inicio de sesión exitoso", jwtService.generateToken(user), UserResponse.from(user));
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("user", "El usuario no existe"));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BusinessException("currentPassword", "La contraseña actual no es correcta");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new BusinessException("newPassword", "La nueva contraseña debe ser diferente a la actual");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    }
}
