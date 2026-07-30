package com.campusguide.personal.ai.atlas.execution.context;

import com.campusguide.personal.ai.atlas.planning.model.ExecutionPlan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ExecutionContextTest {

    @Test
    @DisplayName("ExecutionContext.fromExecutionPlan sets up default contracts")
    void testFromExecutionPlan() {
        ExecutionPlan plan = ExecutionPlan.fallback("plan_test", "Test plan");
        ExecutionContext ctx = ExecutionContext.fromExecutionPlan(plan);

        assertNotNull(ctx);
        assertNotNull(ctx.getContextId());
        assertEquals(plan, ctx.getExecutionPlan());
        assertNotNull(ctx.getObjective());
        assertNotNull(ctx.getConstraints());
        assertNotNull(ctx.getScope());
        assertNotNull(ctx.getSecurityContext());
        assertNotNull(ctx.getRetryPolicies());
        assertNotNull(ctx.getRollbackPolicies());
    }
}
