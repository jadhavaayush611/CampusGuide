package com.campusguide.personal.ai.atlas.orchestration;

import com.campusguide.personal.ai.atlas.orchestration.delegation.TaskAssignment;
import com.campusguide.personal.ai.atlas.orchestration.explainability.CoordinationReason;
import com.campusguide.personal.ai.atlas.orchestration.explainability.DelegationExplanation;
import com.campusguide.personal.ai.atlas.orchestration.explainability.OrchestrationExplanationEngine;
import com.campusguide.personal.ai.atlas.orchestration.replanning.ReplanningDecision;
import com.campusguide.personal.ai.atlas.orchestration.replanning.ReplanningTrigger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrchestrationExplainabilityTest {

    private OrchestrationExplanationEngine explanationEngine;

    @BeforeEach
    void setUp() {
        explanationEngine = new OrchestrationExplanationEngine();
    }

    @Test
    void testDelegationExplanation() {
        TaskAssignment assignment = TaskAssignment.create("task_1", "agent_academic_1", 5, "academic", "Assigned based on capability");
        DelegationExplanation explanation = explanationEngine.explainDelegation(assignment, "CAPABILITY_BASED");

        assertNotNull(explanation);
        assertEquals("agent_academic_1", explanation.getSelectedAgentId());
        assertEquals("CAPABILITY_BASED", explanation.getStrategyUsed());
    }

    @Test
    void testCoordinationAndReplanningExplanation() {
        CoordinationReason coordReason = explanationEngine.explainCoordination("barrier_1", "Synchronization barrier barrier_1 waiting for all stage 1 parallel tasks");
        assertNotNull(coordReason);
        assertTrue(coordReason.getDescription().contains("barrier_1"));

        ReplanningDecision decision = ReplanningDecision.replan(ReplanningTrigger.EXECUTION_FAILURE, "Stage 1 failed", null, null);
        String replanExplain = explanationEngine.explainReplanning(decision);
        assertTrue(replanExplain.contains("EXECUTION_FAILURE"));
    }
}
