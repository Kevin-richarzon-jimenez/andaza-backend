package com.andanza.backend.admin.user;

import com.andanza.backend.common.PageParams;
import com.andanza.backend.exception.BusinessException;
import com.andanza.backend.user.AccountStatus;
import com.andanza.backend.user.Role;
import com.andanza.backend.user.User;
import com.andanza.backend.user.UserRepository;
import com.andanza.backend.user.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminUserService adminUserService;

    private final UUID adminId = UUID.randomUUID();

    private User userWith(UUID id, Role role) {
        User user = new User();
        user.setId(id);
        user.setFirstName("Ana");
        user.setLastName("Lopez");
        user.setEmail("ana@example.com");
        user.setRole(role);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        return user;
    }

    @Test
    void searchIgnoresCaseAndSurroundingSpaces() {
        when(userRepository.search(eq("%ana%"), any())).thenReturn(Page.empty());

        adminUserService.list("  ANA ", new PageParams(null, null));

        verify(userRepository).search(eq("%ana%"), any());
    }

    @Test
    void withoutASearchItListsEveryone() {
        when(userRepository.search(eq("%%"), any())).thenReturn(Page.empty());

        adminUserService.list(null, new PageParams(null, null));

        verify(userRepository).search(eq("%%"), any());
    }

    @Test
    void anAdminCannotBlockThemselves() {
        userWith(adminId, Role.ADMIN);

        assertThatThrownBy(() -> adminUserService.update(adminId, adminId, new AdminUserRequest(Role.ADMIN, AccountStatus.BLOCKED)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("bloquear");
    }

    @Test
    void anAdminCannotRemoveTheirOwnAdminRole() {
        userWith(adminId, Role.ADMIN);

        assertThatThrownBy(() -> adminUserService.update(adminId, adminId, new AdminUserRequest(Role.CUSTOMER, AccountStatus.ACTIVE)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("rol");
    }

    @Test
    void anAdminCanPromoteAndBlockSomeoneElse() {
        UUID otherId = UUID.randomUUID();
        userWith(otherId, Role.CUSTOMER);

        UserResponse response = adminUserService.update(adminId, otherId, new AdminUserRequest(Role.ADMIN, AccountStatus.BLOCKED));

        assertThat(response.role()).isEqualTo(Role.ADMIN);
        assertThat(response.accountStatus()).isEqualTo(AccountStatus.BLOCKED);
    }
}
