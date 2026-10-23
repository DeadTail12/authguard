package com.authguard.authorization;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AbacPolicyEvaluatorTest {

    private final AbacPolicyEvaluator evaluator = new AbacPolicyEvaluator();

    @Test
    void sameRegionAllowsWhenRegionsMatch() {
        AbacContext context = new AbacContext("bob", "EU-WEST", "someone-else", "EU-WEST");
        assertThat(evaluator.evaluate(AbacPolicyEvaluator.SAME_REGION, context)).isTrue();
    }

    @Test
    void sameRegionDeniesWhenRegionsDiffer() {
        AbacContext context = new AbacContext("bob", "EU-WEST", "someone-else", "EU-EAST");
        assertThat(evaluator.evaluate(AbacPolicyEvaluator.SAME_REGION, context)).isFalse();
    }

    @Test
    void sameRegionDeniesWhenSubjectRegionIsMissing() {
        AbacContext context = new AbacContext("bob", null, "someone-else", "EU-WEST");
        assertThat(evaluator.evaluate(AbacPolicyEvaluator.SAME_REGION, context)).isFalse();
    }

    @Test
    void sameRegionDeniesWhenResourceRegionIsMissing() {
        AbacContext context = new AbacContext("bob", "EU-WEST", "someone-else", null);
        assertThat(evaluator.evaluate(AbacPolicyEvaluator.SAME_REGION, context)).isFalse();
    }

    @Test
    void resourceOwnerAllowsWhenSubjectOwnsTheResource() {
        AbacContext context = new AbacContext("bob", "EU-WEST", "bob", "EU-EAST");
        assertThat(evaluator.evaluate(AbacPolicyEvaluator.RESOURCE_OWNER, context)).isTrue();
    }

    @Test
    void resourceOwnerDeniesAccessToSomeoneElsesResource() {
        AbacContext context = new AbacContext("bob", "EU-WEST", "someone-else", "EU-EAST");
        assertThat(evaluator.evaluate(AbacPolicyEvaluator.RESOURCE_OWNER, context)).isFalse();
    }

    @Test
    void unknownRuleNameIsDeniedRatherThanIgnored() {
        AbacContext context = new AbacContext("bob", "EU-WEST", "bob", "EU-WEST");
        assertThat(evaluator.evaluate("SOME_MADE_UP_RULE", context)).isFalse();
    }
}
