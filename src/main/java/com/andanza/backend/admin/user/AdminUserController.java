package com.andanza.backend.admin.user;

import com.andanza.backend.auth.CurrentUser;
import com.andanza.backend.catalog.PageResponse;
import com.andanza.backend.common.PageParams;
import com.andanza.backend.user.UserResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@Tag(name = "Admin - Users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    // search filtra por nombre, apellido o correo (sin distinguir mayúsculas).
    @GetMapping
    public ResponseEntity<PageResponse<UserResponse>> list(@RequestParam(required = false) String search,
                                                           @Valid @ModelAttribute PageParams params) {
        return ResponseEntity.ok(adminUserService.list(search, params));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                                               @Valid @RequestBody AdminUserRequest request) {
        return ResponseEntity.ok(adminUserService.update(CurrentUser.id(jwt), id, request));
    }
}
