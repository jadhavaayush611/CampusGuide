package com.campusguide.personal.ai.atlas.execution.rollback;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionStage;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionUnit;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionUnitType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RollbackPlannerTest {

    @Test
    @DisplayName("RollbackPlanner builds deterministic compensating units in reverse order")
    void testPlanRollback() {
        RollbackPlanner planner = new RollbackPlanner();
        ExecutionContext ctx = ExecutionContext.fromExecutionPlan(null);

        ExecutionUnit actionUnit = ExecutionUnit.builder()
                .unitId("u_act_1")
                .unitType(ExecutionUnitType.MUTATION)
                .title("Write Profile Record")
                .build();

        ExecutionStage stage = ExecutionStage.builder()
                .stageId("s_1")
                .executionUnits(List.of(actionUnit))
                .build();

        ExecutableWorkflow wf = ExecutableWorkflow.builder()
                .workflowId("wf_rb_test")
                .stages(List.of(stage))
                .build();

        RollbackPlan plan = planner.planRollback(ctx, wf);

        assertNotNull(plan);
        assertEquals("wf_rb_test", plan.getWorkflowId());
        assertTrue(plan.isDeterministic());
        assertEquals(1, plan.getRollbackUnits().size());
        assertTrue(plan.getRollbackUnits().get(0).getTitle().contains("Undo / Rollback"));
    }
}
