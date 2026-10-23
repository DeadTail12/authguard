package com.authguard.authorization;

import com.authguard.audit.AuthorizationAuditLogger;
import com.authguard.audit.Decision;
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
    private final AuthorizationAuditLogger auditLogger;

    public AuthorizationService(UserRoleRepository userRoleRepository, AuthorizationAuditLogger auditLogger) {
        this.userRoleRepository = userRoleRepository;
        this.auditLogger = auditLogger;
    }

    public boolean hasPermission(String keycloakUserId, String permissionName) {
        if (keycloakUserId == null || permissionName == null) {
            return false;
        }
        return userRoleRepository.findByKeycloakUserId(keycloakUserId).stream()
                .flatMap(userRole -> userRole.getRole().getPermissions().stream())
                .anyMatch(permission -> permission.getName().equals(permissionName));
    }

    /** Same check, but throws so a call site can't accidentally ignore a denial — and audits the outcome. */
    public void requirePermission(String keycloakUserId, String permissionName) {
        boolean allowed = hasPermission(keycloakUserId, permissionName);
        auditLogger.record(
                keycloakUserId,
                permissionName,
                "endpoint",
                allowed ? Decision.ALLOW : Decision.DENY,
                allowed ? "an assigned role grants this permission" : "no assigned role grants this permission");

        if (!allowed) {
            throw new AccessDeniedException("Missing permission: " + permissionName);
        }
    }
}
