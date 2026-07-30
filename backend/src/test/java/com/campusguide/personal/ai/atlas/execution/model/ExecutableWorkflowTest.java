package com.campusguide.personal.ai.atlas.execution.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ExecutableWorkflowTest {

    @Test
    @DisplayName("ExecutableWorkflow fallback creates degraded workflow")
    void testFallbackCreation() {
        ExecutableWorkflow wf = ExecutableWorkflow.fallback("wf_test_fb", "Test rationale");
        assertNotNull(wf);
        assertEquals("wf_test_fb", wf.getWorkflowId());
        assertEquals(WorkflowStatus.DEGRADED, wf.getStatus());
        assertFalse(wf.getStages().isEmpty());
        assertEquals(1, wf.getStages().size());
        assertEquals("Fallback Stage", wf.getStages().get(0).getStageName());
    }

    @Test
    @DisplayName("ExecutionMetadata createDefault populates timestamps and checksum")
    void testMetadataCreation() {
        ExecutionMetadata meta = ExecutionMetadata.createDefault("wf_1", "plan_1", "ctx_1");
        assertNotNull(meta);
        assertEquals("wf_1", meta.getWorkflowId());
        assertEquals("plan_1", meta.getPlanId());
        assertEquals("ctx_1", meta.getContextId());
        assertNotNull(meta.getPreparedAt());
        assertNotNull(meta.getChecksum());
    }
}
