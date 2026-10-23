package com.authguard.service;

import com.authguard.authorization.AuthorizationService;
import com.authguard.dto.PermissionResponse;
import com.authguard.repository.PermissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PermissionService {

    private static final String REQUIRED_PERMISSION = "policies:manage";

    private final PermissionRepository permissionRepository;
    private final AuthorizationService authorizationService;

    public PermissionService(PermissionRepository permissionRepository, AuthorizationService authorizationService) {
        this.permissionRepository = permissionRepository;
        this.authorizationService = authorizationService;
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> listPermissions(String keycloakUserId) {
        authorizationService.requirePermission(keycloakUserId, REQUIRED_PERMISSION);
        return permissionRepository.findAll().stream()
                .map(permission -> new PermissionResponse(permission.getId(), permission.getName()))
                .toList();
    }
}
