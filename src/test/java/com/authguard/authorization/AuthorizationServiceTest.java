package com.authguard.authorization;

import com.authguard.entity.Permission;
import com.authguard.entity.Role;
import com.authguard.entity.UserRole;
import com.authguard.repository.UserRoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthorizationServiceTest {

    private final UserRoleRepository userRoleRepository = mock(UserRoleRepository.class);
    private final AuthorizationService authorizationService = new AuthorizationService(userRoleRepository);

    @Test
    void grantsWhenOneOfTheUsersRolesHasThePermission() {
        Role admin = new Role("ADMIN");
        admin.getPermissions().add(new Permission("policies:manage"));
        when(userRoleRepository.findByKeycloakUserId("alice")).thenReturn(List.of(new UserRole("alice", admin)));

        assertThat(authorizationService.hasPermission("alice", "policies:manage")).isTrue();
    }

    @Test
    void deniesWhenRoleDoesNotCarryThePermission() {
        Role manager = new Role("MANAGER");
        manager.getPermissions().add(new Permission("orders:read"));
        when(userRoleRepository.findByKeycloakUserId("bob")).thenReturn(List.of(new UserRole("bob", manager)));

        assertThat(authorizationService.hasPermission("bob", "policies:manage")).isFalse();
    }

    @Test
    void deniesWhenUserHasNoRolesAtAll() {
        when(userRoleRepository.findByKeycloakUserId("ghost")).thenReturn(List.of());

        assertThat(authorizationService.hasPermission("ghost", "policies:manage")).isFalse();
    }

    @Test
    void requirePermissionThrowsInsteadOfReturningFalse() {
        when(userRoleRepository.findByKeycloakUserId("bob")).thenReturn(List.of());

        assertThatThrownBy(() -> authorizationService.requirePermission("bob", "policies:manage"))
                .isInstanceOf(AccessDeniedException.class);
    }
}
