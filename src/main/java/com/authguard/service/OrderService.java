package com.authguard.service;

import com.authguard.audit.AuthorizationAuditLogger;
import com.authguard.audit.Decision;
import com.authguard.authorization.AbacContext;
import com.authguard.authorization.AbacPolicyEvaluator;
import com.authguard.authorization.AttributeProvider;
import com.authguard.authorization.AuthorizationService;
import com.authguard.dto.OrderResponse;
import com.authguard.entity.Order;
import com.authguard.exception.OrderNotFoundException;
import com.authguard.repository.OrderRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Two layers of authorization, in order: RBAC first ("can this user call orders:read at
 * all"), then ABAC for the specific order ("is THIS order theirs to see"). A permission
 * alone never grants access to an arbitrary order id — that would be the IDOR hole ABAC
 * exists to close. {@code orders:manage} is treated as an explicit admin override that
 * skips the per-order check, same as a real admin console would.
 */
@Service
public class OrderService {

    private static final String READ_PERMISSION = "orders:read";
    private static final String MANAGE_PERMISSION = "orders:manage";

    private final OrderRepository orderRepository;
    private final AuthorizationService authorizationService;
    private final AttributeProvider attributeProvider;
    private final AbacPolicyEvaluator abacPolicyEvaluator;
    private final AuthorizationAuditLogger auditLogger;

    public OrderService(
            OrderRepository orderRepository,
            AuthorizationService authorizationService,
            AttributeProvider attributeProvider,
            AbacPolicyEvaluator abacPolicyEvaluator,
            AuthorizationAuditLogger auditLogger) {
        this.orderRepository = orderRepository;
        this.authorizationService = authorizationService;
        this.attributeProvider = attributeProvider;
        this.abacPolicyEvaluator = abacPolicyEvaluator;
        this.auditLogger = auditLogger;
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(String keycloakUserId, Long orderId) {
        authorizationService.requirePermission(keycloakUserId, READ_PERMISSION);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        String resource = "order:" + orderId;
        boolean allowed;
        String reason;

        if (authorizationService.hasPermission(keycloakUserId, MANAGE_PERMISSION)) {
            allowed = true;
            reason = "orders:manage grants access to any order";
        } else {
            String matchedRule = matchingAbacRule(keycloakUserId, order);
            allowed = matchedRule != null;
            reason = allowed
                    ? "ABAC rule " + matchedRule + " matched"
                    : "no ABAC rule matched (different region, not the owner)";
        }

        auditLogger.record(keycloakUserId, "view-order", resource, allowed ? Decision.ALLOW : Decision.DENY, reason);

        if (!allowed) {
            throw new AccessDeniedException("Order " + orderId + " is not in your region and not yours");
        }

        return toResponse(order);
    }

    /** Returns the name of the first ABAC rule that allows access, or {@code null} if none does. */
    private String matchingAbacRule(String keycloakUserId, Order order) {
        AbacContext context = new AbacContext(
                keycloakUserId,
                attributeProvider.getSubjectRegion(keycloakUserId),
                order.getOwnerKeycloakUserId(),
                order.getRegion());

        if (abacPolicyEvaluator.evaluate(AbacPolicyEvaluator.RESOURCE_OWNER, context)) {
            return AbacPolicyEvaluator.RESOURCE_OWNER;
        }
        if (abacPolicyEvaluator.evaluate(AbacPolicyEvaluator.SAME_REGION, context)) {
            return AbacPolicyEvaluator.SAME_REGION;
        }
        return null;
    }

    private static OrderResponse toResponse(Order order) {
        return new OrderResponse(order.getId(), order.getRegion(), order.getOwnerKeycloakUserId(), order.getDescription());
    }
}
