package com.authguard.controller;

import com.authguard.dto.RoleResponse;
import com.authguard.security.SecurityConfig;
import com.authguard.service.RoleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies the HTTP-level outcomes that matter for RBAC: 401 with no token, 403 when the
 * service layer denies the permission, 200 when it allows it. RoleService is mocked here
 * because the actual permission-resolution logic is covered by AuthorizationServiceTest
 * (unit) and AuthorizationServiceIntegrationTest (real Postgres + seeded data).
 */
@WebMvcTest(RoleController.class)
@Import(SecurityConfig.class)
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private RoleService roleService;

    @Test
    void rejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void forbidsUserWithoutPolicyManagePermission() throws Exception {
        when(roleService.listRoles(any())).thenThrow(new AccessDeniedException("Missing permission: policies:manage"));

        mockMvc.perform(get("/api/v1/roles").with(jwt()))
                .andExpect(status().isForbidden());
    }

    @Test
    void allowsUserWithPolicyManagePermission() throws Exception {
        when(roleService.listRoles(any())).thenReturn(List.of(new RoleResponse(1L, "ADMIN", Set.of("policies:manage"))));

        mockMvc.perform(get("/api/v1/roles").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("ADMIN"));
    }
}
