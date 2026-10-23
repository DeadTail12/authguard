package com.authguard.authorization;

/**
 * Everything an ABAC rule might need, gathered up front. Any field can be {@code null}
 * when that attribute isn't known (e.g. the user never got a region assigned) — rules
 * must treat a missing attribute as "cannot prove access", not as a wildcard match.
 */
public record AbacContext(
        String subjectId,
        String subjectRegion,
        String resourceOwnerId,
        String resourceRegion) {
}
