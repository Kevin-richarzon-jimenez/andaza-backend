package com.andanza.backend.admin.user;

import com.andanza.backend.exception.BusinessException;
import org.springframework.stereotype.Service;

// Sin BD: valida la operación y la confirma; no actualiza ningún registro real.
//
// DEMO ONLY: la regla de abajo (no tocar al admin principal) es solo una
// regla de negocio de ejemplo sobre un id fijo -- NO es autorización real,
// no verifica quién está llamando. Cualquiera puede mandar cualquier otro
// id y el cambio "se aplica" igual (aunque en este demo no persiste nada).
// TODO (auth): cuando exista autenticación real, esta validación debe
// hacerse contra el usuario autenticado, no contra un id fijo de ejemplo,
// y el resto de estos endpoints de Admin deben quedar detrás de un rol.
@Service
public class AdminUserService {

    private static final String DEMO_MAIN_ADMIN_ID = "u-admin-001";

    public String update(String userId, AdminUserRequest request) {
        boolean isMainAdmin = DEMO_MAIN_ADMIN_ID.equals(userId);
        if (isMainAdmin && request.accountStatus() == AccountStatus.BLOCKED) {
            throw new BusinessException("accountStatus", "No puedes bloquear al administrador principal");
        }
        if (isMainAdmin && request.role() == Role.CUSTOMER) {
            throw new BusinessException("role", "No puedes quitarle el rol de administrador al administrador principal");
        }
        // TODO (BD): actualizar rol y estado reales del usuario.
        return "Usuario " + userId + " actualizado: rol=" + request.role() + ", estado=" + request.accountStatus();
    }
}
