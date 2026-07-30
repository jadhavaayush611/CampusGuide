package com.campusguide.personal.ai.atlas.planning.explanation;

import com.campusguide.personal.ai.atlas.decision.model.DecisionOutcome;
import com.campusguide.personal.ai.atlas.planning.context.PlanningContext;
import com.campusguide.personal.ai.atlas.planning.decomposition.GoalHierarchy;
import com.campusguide.personal.ai.atlas.planning.graph.TaskGraph;
import com.campusguide.personal.ai.atlas.planning.model.PlanningGoal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlanningExplanationEngineTest {

    @Test
    @DisplayName("ExplanationEngine should generate structured explanation result")
    void testExplanationEngine() {
        PlanningExplanationEngine engine = new PlanningExplanationEngine();

        PlanningContext context = PlanningContext.fromDecisionOutcome(DecisionOutcome.fallback("out_exp", "Explanation rationale"));
        GoalHierarchy hierarchy = GoalHierarchy.builder().rootGoal(PlanningGoal.builder().goalId("g1").title("Goal 1").build()).build();
        TaskGraph graph = new TaskGraph();

        PlanningExplanation explanation = engine.generateExplanation(context, hierarchy, graph, null, null, null);

        assertThat(explanation).isNotNull();
        assertThat(explanation.getPrimaryRationale()).contains("Explanation rationale");
        assertThat(explanation.getReasons()).isNotEmpty();
        assertThat(explanation.getEvidenceList()).isNotEmpty();
    }
}
