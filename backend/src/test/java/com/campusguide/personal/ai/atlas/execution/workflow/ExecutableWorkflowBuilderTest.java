package com.campusguide.personal.ai.atlas.execution.workflow;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;
import com.campusguide.personal.ai.atlas.execution.model.WorkflowStatus;
import com.campusguide.personal.ai.atlas.planning.model.ExecutionPlan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ExecutableWorkflowBuilderTest {

    private ExecutableWorkflowBuilder builder;

    @BeforeEach
    void setUp() {
        WorkflowAssembler assembler = new WorkflowAssembler();
        WorkflowRegistry registry = new WorkflowRegistry();
        builder = new ExecutableWorkflowBuilder(assembler, registry);
    }

    @Test
    @DisplayName("ExecutableWorkflowBuilder builds workflow from ExecutionContext")
    void testBuildWorkflow() {
        ExecutionPlan plan = ExecutionPlan.fallback("plan_bld", "Test build");
        ExecutionContext ctx = ExecutionContext.fromExecutionPlan(plan);

        ExecutableWorkflow wf = builder.buildWorkflow(ctx);

        assertNotNull(wf);
        assertNotNull(wf.getWorkflowId());
        assertEquals("plan_bld", wf.getPlanId());
        assertFalse(wf.getStages().isEmpty());
        assertFalse(wf.getCheckpoints().isEmpty());
        assertNotNull(wf.getContract());
        assertEquals(WorkflowStatus.PREPARED, wf.getStatus());
    }

    @Test
    @DisplayName("ExecutableWorkflowBuilder handles null ExecutionContext")
    void testBuildWorkflowNullContext() {
        ExecutableWorkflow wf = builder.buildWorkflow(null);
        assertNotNull(wf);
        assertEquals(WorkflowStatus.DEGRADED, wf.getStatus());
    }
}
