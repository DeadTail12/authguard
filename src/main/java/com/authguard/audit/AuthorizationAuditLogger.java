package com.authguard.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Writes one structured line per authorization decision: who asked, for what action, on
 * what resource, ALLOW or DENY, and why. Deliberately logs only identifiers the subject
 * already knows about itself (its own Keycloak user id) — never passwords, raw JWTs,
 * refresh tokens, or anything else that would turn a log file into a secret store.
 * On a separate logger name so it can be routed/retained differently from app logs.
 */
@Component
public class AuthorizationAuditLogger {

    private static final Logger log = LoggerFactory.getLogger("AUTHORIZATION_AUDIT");

    public void record(String subjectId, String action, String resource, Decision decision, String reason) {
        log.info("timestamp={} subject={} action={} resource={} decision={} reason=\"{}\"",
                Instant.now(), subjectId, action, resource, decision, reason);
    }
}
