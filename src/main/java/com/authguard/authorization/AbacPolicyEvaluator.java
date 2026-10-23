package com.authguard.authorization;

import org.springframework.stereotype.Service;

/**
 * Evaluates one named rule against an {@link AbacContext}. Deliberately NOT a generic
 * policy language: rules are a small, fixed set of Java predicates, never a stored
 * expression or anything resembling eval() of untrusted data. Anything that isn't one
 * of the known rule names — or where a required attribute is missing — resolves to
 * {@code false}. This component only answers "does this one rule hold"; combining
 * several rules into an overall allow/deny decision is the caller's job.
 */
@Service
public class AbacPolicyEvaluator {

    public static final String SAME_REGION = "SAME_REGION";
    public static final String RESOURCE_OWNER = "RESOURCE_OWNER";

    public boolean evaluate(String rule, AbacContext context) {
        return switch (rule) {
            case SAME_REGION -> sameRegion(context);
            case RESOURCE_OWNER -> isResourceOwner(context);
            default -> false;
        };
    }

    private boolean sameRegion(AbacContext context) {
        return context.subjectRegion() != null
                && context.resourceRegion() != null
                && context.subjectRegion().equals(context.resourceRegion());
    }

    private boolean isResourceOwner(AbacContext context) {
        return context.subjectId() != null
                && context.resourceOwnerId() != null
                && context.subjectId().equals(context.resourceOwnerId());
    }
}
