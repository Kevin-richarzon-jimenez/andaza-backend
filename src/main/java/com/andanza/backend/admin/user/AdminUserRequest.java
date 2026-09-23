package com.andanza.backend.admin.user;

import jakarta.validation.constraints.NotNull;

public record AdminUserRequest(
        @NotNull(message = "El rol es obligatorio")
        Role role,

        @NotNull(message = "El estado es obligatorio")
        AccountStatus accountStatus
) {
}
