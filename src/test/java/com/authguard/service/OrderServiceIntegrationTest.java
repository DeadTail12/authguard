package com.authguard.service;

import com.authguard.authorization.AbacPolicyEvaluator;
import com.authguard.authorization.AttributeProvider;
import com.authguard.authorization.AuthorizationService;
import com.authguard.dto.OrderResponse;
import com.authguard.repository.OrderRepository;
import com.authguard.repository.UserAttributesRepository;
import com.authguard.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.access.AccessDeniedException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Exercises OrderService's RBAC+ABAC decision against the real V1/V2 migrations and
 * seed data on a real PostgreSQL container — the automated equivalent of the manual
 * curl-with-a-real-Keycloak-token checks done during development.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class OrderServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    private static final String ALICE_SUB = "11111111-1111-4111-8111-111111111111"; // ADMIN
    private static final String BOB_SUB = "22222222-2222-4222-8222-222222222222"; // MANAGER, region EU-WEST
    private static final long ORDER_SAME_REGION_NOT_OWNED = 1L;
    private static final long ORDER_DIFFERENT_REGION_OWNED_BY_BOB = 2L;
    private static final long ORDER_DIFFERENT_REGION_NOT_OWNED = 3L;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private UserAttributesRepository userAttributesRepository;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        AuthorizationService authorizationService = new AuthorizationService(userRoleRepository);
        AttributeProvider attributeProvider = new AttributeProvider(userAttributesRepository);
        orderService = new OrderService(orderRepository, authorizationService, attributeProvider, new AbacPolicyEvaluator());
    }

    @Test
    void managerCanViewOrderInOwnRegionEvenWithoutOwningIt() {
        OrderResponse order = orderService.getOrder(BOB_SUB, ORDER_SAME_REGION_NOT_OWNED);
        assertThat(order.id()).isEqualTo(ORDER_SAME_REGION_NOT_OWNED);
    }

    @Test
    void managerCanViewOwnOrderOutsideTheirRegion() {
        OrderResponse order = orderService.getOrder(BOB_SUB, ORDER_DIFFERENT_REGION_OWNED_BY_BOB);
        assertThat(order.id()).isEqualTo(ORDER_DIFFERENT_REGION_OWNED_BY_BOB);
    }

    @Test
    void managerCannotViewOrderOutsideRegionTheyDoNotOwn() {
        assertThatThrownBy(() -> orderService.getOrder(BOB_SUB, ORDER_DIFFERENT_REGION_NOT_OWNED))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void adminBypassesAbacViaOrdersManage() {
        OrderResponse order = orderService.getOrder(ALICE_SUB, ORDER_DIFFERENT_REGION_NOT_OWNED);
        assertThat(order.id()).isEqualTo(ORDER_DIFFERENT_REGION_NOT_OWNED);
    }

    @Test
    void userWithoutOrdersReadPermissionIsDeniedBeforeAbacEvenApplies() {
        assertThatThrownBy(() -> orderService.getOrder("no-such-user", ORDER_SAME_REGION_NOT_OWNED))
                .isInstanceOf(AccessDeniedException.class);
    }
}
