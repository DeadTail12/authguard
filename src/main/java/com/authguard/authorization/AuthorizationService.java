package com.authguard.authorization;

import com.authguard.repository.UserRoleRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

/**
 * The single place that turns "which roles does this user have" into "can they do X".
 * Deliberately fail-closed: a user with no roles, an unknown user, or a role that
 * doesn't carry the requested permission all resolve to {@code false}, never to an
 * exception that could be mistaken for "allowed".
 */
@Service
public class AuthorizationService {

    private final UserRoleRepository userRoleRepository;

    public AuthorizationService(UserRoleRepository userRoleRepository) {
        this.userRoleRepository = userRoleRepository;
    }

    public boolean hasPermission(String keycloakUserId, String permissionName) {
        if (keycloakUserId == null || permissionName == null) {
            return false;
        }
        return userRoleRepository.findByKeycloakUserId(keycloakUserId).stream()
                .flatMap(userRole -> userRole.getRole().getPermissions().stream())
                .anyMatch(permission -> permission.getName().equals(permissionName));
    }

    /** Same check, but throws so a call site can't accidentally ignore a denial. */
    public void requirePermission(String keycloakUserId, String permissionName) {
        if (!hasPermission(keycloakUserId, permissionName)) {
            throw new AccessDeniedException("Missing permission: " + permissionName);
        }
    }
}
