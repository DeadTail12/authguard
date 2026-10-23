package com.authguard.service;

import com.authguard.authorization.AuthorizationService;
import com.authguard.dto.CreateRoleRequest;
import com.authguard.dto.RoleResponse;
import com.authguard.entity.Permission;
import com.authguard.entity.Role;
import com.authguard.exception.RoleAlreadyExistsException;
import com.authguard.exception.UnknownPermissionException;
import com.authguard.repository.PermissionRepository;
import com.authguard.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/** Role/permission catalog management — an admin-only operation, gated by {@code policies:manage}. */
@Service
public class RoleService {

    private static final String REQUIRED_PERMISSION = "policies:manage";

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final AuthorizationService authorizationService;

    public RoleService(
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            AuthorizationService authorizationService) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.authorizationService = authorizationService;
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> listRoles(String keycloakUserId) {
        authorizationService.requirePermission(keycloakUserId, REQUIRED_PERMISSION);
        return roleRepository.findAll().stream().map(RoleService::toResponse).toList();
    }

    @Transactional
    public RoleResponse createRole(String keycloakUserId, CreateRoleRequest request) {
        authorizationService.requirePermission(keycloakUserId, REQUIRED_PERMISSION);

        if (roleRepository.findByName(request.name()).isPresent()) {
            throw new RoleAlreadyExistsException(request.name());
        }

        List<Permission> permissions = permissionRepository.findByNameIn(request.permissionNames());
        if (permissions.size() != request.permissionNames().size()) {
            List<String> known = permissions.stream().map(Permission::getName).toList();
            List<String> unknown = request.permissionNames().stream()
                    .filter(name -> !known.contains(name))
                    .toList();
            throw new UnknownPermissionException(unknown);
        }

        Role role = new Role(request.name());
        role.getPermissions().addAll(permissions);
        return toResponse(roleRepository.save(role));
    }

    private static RoleResponse toResponse(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getPermissions().stream().map(Permission::getName).collect(Collectors.toSet()));
    }
}
