package com.authguard.authorization;

import com.authguard.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs the real V1 Flyway migration (schema + RBAC seed data) against an actual
 * PostgreSQL container — not H2 — then checks the seeded users end up with the
 * permissions the migration says they should have. This is the automated stand-in
 * for the manual curl-with-a-real-Keycloak-token check done during development.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class AuthorizationServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    private static final String ALICE_SUB = "11111111-1111-4111-8111-111111111111";
    private static final String BOB_SUB = "22222222-2222-4222-8222-222222222222";

    @Autowired
    private UserRoleRepository userRoleRepository;

    private AuthorizationService authorizationService;

    @BeforeEach
    void setUp() {
        authorizationService = new AuthorizationService(userRoleRepository);
    }

    @Test
    void seededAdminHasPolicyManagePermission() {
        assertThat(authorizationService.hasPermission(ALICE_SUB, "policies:manage")).isTrue();
    }

    @Test
    void seededManagerLacksPolicyManagePermission() {
        assertThat(authorizationService.hasPermission(BOB_SUB, "policies:manage")).isFalse();
    }

    @Test
    void seededManagerHasOrdersPermissions() {
        assertThat(authorizationService.hasPermission(BOB_SUB, "orders:read")).isTrue();
        assertThat(authorizationService.hasPermission(BOB_SUB, "orders:manage")).isTrue();
    }

    @Test
    void unknownUserHasNoPermissions() {
        assertThat(authorizationService.hasPermission("no-such-user", "users:read")).isFalse();
    }
}
