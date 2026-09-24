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

import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
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
    public AuthResponse login(LoginRequest request) {
        // El mismo mensaje para correo inexistente y contraseña incorrecta: no revela cuáles correos están registrados.
        User user = userRepository.findByEmailIgnoreCase(request.email().trim())
                .filter(found -> passwordEncoder.matches(request.password(), found.getPasswordHash()))
                .orElseThrow(() -> new UnauthorizedException("password", "Correo o contraseña incorrectos"));
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
