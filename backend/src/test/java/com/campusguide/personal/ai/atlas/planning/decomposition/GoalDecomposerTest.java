package com.campusguide.personal.ai.atlas.planning.decomposition;

import com.campusguide.personal.ai.atlas.decision.model.DecisionOutcome;
import com.campusguide.personal.ai.atlas.planning.context.PlanningContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GoalDecomposerTest {

    private GoalDecomposer decomposer;
    private GoalRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new GoalRegistry();
        decomposer = new GoalDecomposer(registry);
    }

    @Test
    @DisplayName("Decompose should create root goal and recursive sub-goals")
    void testDecompose() {
        DecisionOutcome outcome = DecisionOutcome.fallback("out_decomp", "Test goal rationale");
        PlanningContext context = PlanningContext.fromDecisionOutcome(outcome);

        GoalHierarchy hierarchy = decomposer.decompose(context);

        assertThat(hierarchy).isNotNull();
        assertThat(hierarchy.getRootGoal()).isNotNull();
        assertThat(hierarchy.getSubGoals()).isNotEmpty();
        assertThat(hierarchy.getTotalGoalCount()).isGreaterThan(1);
        assertThat(hierarchy.getMandatoryGoalCount()).isGreaterThan(0);
    }
}
