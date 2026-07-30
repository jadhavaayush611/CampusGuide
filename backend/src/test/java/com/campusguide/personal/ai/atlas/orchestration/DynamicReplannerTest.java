package com.campusguide.personal.ai.atlas.orchestration;

import com.campusguide.personal.ai.atlas.execution.engine.ExecutionPreparationEngine;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;
import com.campusguide.personal.ai.atlas.orchestration.replanning.DynamicReplanner;
import com.campusguide.personal.ai.atlas.orchestration.replanning.ReplanningDecision;
import com.campusguide.personal.ai.atlas.orchestration.replanning.ReplanningPolicy;
import com.campusguide.personal.ai.atlas.orchestration.replanning.ReplanningTrigger;
import com.campusguide.personal.ai.atlas.planning.context.PlanningContext;
import com.campusguide.personal.ai.atlas.planning.engine.PlanningEngine;
import com.campusguide.personal.ai.atlas.planning.model.ExecutionPlan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DynamicReplannerTest {

    @Mock
    private PlanningEngine planningEngine;
    @Mock
    private ExecutionPreparationEngine preparationEngine;

    private DynamicReplanner replanner;

    @BeforeEach
    void setUp() {
        replanner = new DynamicReplanner(planningEngine, preparationEngine);
    }

    @Test
    void testEvaluateAndReplanSuccess() {
        PlanningContext context = PlanningContext.builder().contextId("pctx_replan").build();
        ExecutionPlan newPlan = ExecutionPlan.builder().planId("plan_new").build();
        ExecutableWorkflow newWf = ExecutableWorkflow.builder().workflowId("wf_new").build();

        when(planningEngine.generatePlan(any(PlanningContext.class))).thenReturn(newPlan);
        when(preparationEngine.prepareWorkflow(any(ExecutionPlan.class))).thenReturn(newWf);

        ReplanningDecision decision = replanner.evaluateAndReplan(
                ReplanningTrigger.EXECUTION_FAILURE, "Stage 1 failed", context, ReplanningPolicy.defaultPolicy());

        assertNotNull(decision);
        assertTrue(decision.isReplanRequired());
        assertEquals(ReplanningTrigger.EXECUTION_FAILURE, decision.getTrigger());
        assertEquals("plan_new", decision.getUpdatedPlan().getPlanId());
        assertEquals("wf_new", decision.getUpdatedWorkflow().getWorkflowId());
    }
}
