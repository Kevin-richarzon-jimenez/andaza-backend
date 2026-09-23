package com.andanza.backend.auth;

import com.andanza.backend.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    private static final String DEMO_EMAIL = "demo@andanza.co";
    private static final String DEMO_PASSWORD = "Demo1234";

    public AuthResponse login(LoginRequest request) {
        // TODO (BD): reemplazar por búsqueda real de usuario por correo
        // y verificación de hash de contraseña.
        boolean validCredentials = DEMO_EMAIL.equalsIgnoreCase(request.email())
                && DEMO_PASSWORD.equals(request.password());

        if (!validCredentials) {
            throw new BusinessException("password", "Correo o contraseña incorrectos");
        }

        String demoToken = "token-demo-" + UUID.randomUUID();
        return new AuthResponse("Inicio de sesión exitoso", request.email(), demoToken);
    }

    public AuthResponse register(RegisterRequest request) {
        // La coincidencia de password/confirmPassword ya la valida
        // @FieldsMatch a nivel de formulario (400 antes de llegar aquí).

        // TODO (BD): reemplazar por verificación real de correo existente.
        if (DEMO_EMAIL.equalsIgnoreCase(request.email())) {
            throw new BusinessException("email", "Ya existe una cuenta registrada con este correo");
        }

        // TODO (BD): guardar el usuario (con la contraseña hasheada, nunca en texto plano)
        String demoToken = "token-demo-" + UUID.randomUUID();
        return new AuthResponse("Cuenta creada correctamente", request.email(), demoToken);
    }

    public void changePassword(ChangePasswordRequest request) {
        // TODO (BD): comparar currentPassword contra el hash real del usuario autenticado.
        if (!DEMO_PASSWORD.equals(request.currentPassword())) {
            throw new BusinessException("currentPassword", "La contraseña actual no es correcta");
        }

        if (request.currentPassword().equals(request.newPassword())) {
            throw new BusinessException("newPassword", "La nueva contraseña debe ser diferente a la actual");
        }

        // La coincidencia newPassword/confirmNewPassword ya la valida
        // @FieldsMatch a nivel de formulario.

        // TODO (BD): persistir el nuevo hash de contraseña para el usuario autenticado.
    }
}
