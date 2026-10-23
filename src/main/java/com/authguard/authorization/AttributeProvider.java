package com.authguard.authorization;

import com.authguard.entity.UserAttributes;
import com.authguard.repository.UserAttributesRepository;
import org.springframework.stereotype.Service;

/**
 * Looks up the attributes a policy might need. Kept separate from {@link AbacPolicyEvaluator}
 * on purpose: this class knows WHERE attributes come from, the evaluator only knows what to
 * do with them. A user AuthGuard has no record for simply has no region — {@code null}, not
 * an exception, so the evaluator's "missing attribute" handling kicks in naturally.
 */
@Service
public class AttributeProvider {

    private final UserAttributesRepository userAttributesRepository;

    public AttributeProvider(UserAttributesRepository userAttributesRepository) {
        this.userAttributesRepository = userAttributesRepository;
    }

    public String getSubjectRegion(String keycloakUserId) {
        return userAttributesRepository.findById(keycloakUserId)
                .map(UserAttributes::getRegion)
                .orElse(null);
    }
}
