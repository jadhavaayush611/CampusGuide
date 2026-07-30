package com.campusguide.personal.ai.atlas.planning.context;

import com.campusguide.personal.ai.atlas.decision.context.DecisionContext;

import com.campusguide.personal.ai.atlas.decision.model.DecisionCandidate;
import com.campusguide.personal.ai.atlas.decision.model.DecisionOutcome;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlanningContextTest {

    @Test
    @DisplayName("PlanningContext should build correctly from DecisionOutcome")
    void testFromDecisionOutcome() {
        DecisionOutcome outcome = DecisionOutcome.fallback("out_test_123", "Decision fallback test");

        PlanningContext pctx = PlanningContext.fromDecisionOutcome(outcome);

        assertThat(pctx).isNotNull();
        assertThat(pctx.getDecisionOutcome()).isEqualTo(outcome);
        assertThat(pctx.getObjective()).isNotNull();
        assertThat(pctx.getObjective().getPrimaryGoal()).contains("Decision fallback test");
        assertThat(pctx.getConstraints()).isNotNull();
        assertThat(pctx.getScope()).isNotNull();
    }

    @Test
    @DisplayName("PlanningContext from DecisionContext should copy user and environmental context")
    void testFromDecisionContext() {
        DecisionContext decCtx = DecisionContext.builder()
                .userId("user_99")
                .build();

        DecisionOutcome outcome = DecisionOutcome.fallback("out_test_456", "Test outcome");

        PlanningContext pctx = PlanningContext.fromDecisionContext(decCtx, outcome);

        assertThat(pctx.getUserId()).isEqualTo("user_99");
        assertThat(pctx.getDecisionContext()).isEqualTo(decCtx);
    }
}
