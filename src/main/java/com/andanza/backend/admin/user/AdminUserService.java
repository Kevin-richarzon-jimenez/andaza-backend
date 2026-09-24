package com.andanza.backend.admin.user;

import com.andanza.backend.exception.BusinessException;
import com.andanza.backend.exception.NotFoundException;
import com.andanza.backend.user.AccountStatus;
import com.andanza.backend.user.Role;
import com.andanza.backend.user.User;
import com.andanza.backend.user.UserRepository;
import com.andanza.backend.user.UserResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AdminUserService {

    private final UserRepository userRepository;

    public AdminUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserResponse update(UUID adminId, UUID userId, AdminUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("id", "El usuario indicado no existe"));
        // Regla anti-bloqueo: un administrador no puede quitarse a sí mismo el acceso.
        if (adminId.equals(userId)) {
            if (request.accountStatus() == AccountStatus.BLOCKED) {
                throw new BusinessException("accountStatus", "No puedes bloquear tu propia cuenta");
            }
            if (request.role() != Role.ADMIN) {
                throw new BusinessException("role", "No puedes quitarte el rol de administrador a ti mismo");
            }
        }
        user.setRole(request.role());
        user.setAccountStatus(request.accountStatus());
        return UserResponse.from(user);
    }
}
