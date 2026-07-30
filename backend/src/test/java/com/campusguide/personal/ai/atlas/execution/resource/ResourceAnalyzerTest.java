package com.campusguide.personal.ai.atlas.execution.resource;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;
import com.campusguide.personal.ai.atlas.planning.model.ExecutionPlan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ResourceAnalyzerTest {

    @Test
    @DisplayName("ResourceAnalyzer performs dry-run resource analysis without allocating resources")
    void testResourceAnalysis() {
        ResourceAnalyzer analyzer = new ResourceAnalyzer();
        ExecutionPlan plan = ExecutionPlan.fallback("plan_res", "Test resource analysis");
        ExecutionContext ctx = ExecutionContext.fromExecutionPlan(plan);
        ExecutableWorkflow wf = ExecutableWorkflow.fallback("wf_res", "Test workflow");

        ResourceAllocation allocation = analyzer.analyzeResources(ctx, wf);

        assertNotNull(allocation);
        assertNotNull(allocation.getAllocationId());
        assertFalse(allocation.getRequirements().isEmpty());
        assertTrue(allocation.isAllRequirementsSatisfied());
        assertEquals("DRY_RUN_ANALYSIS", allocation.getAllocationStrategy());
    }
}
