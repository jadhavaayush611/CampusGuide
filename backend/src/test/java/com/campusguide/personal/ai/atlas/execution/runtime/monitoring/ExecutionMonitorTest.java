package com.campusguide.personal.ai.atlas.execution.runtime.monitoring;

import com.campusguide.personal.ai.atlas.execution.runtime.events.ExecutionEvent;
import com.campusguide.personal.ai.atlas.execution.runtime.events.WorkflowEvent;
import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExecutionMonitorTest {

    private ExecutionMonitor monitor;

    @BeforeEach
    void setUp() {
        RuntimeMetrics metrics = new RuntimeMetrics(null);
        HealthMonitor health = new HealthMonitor();
        monitor = new ExecutionMonitor(metrics, health);
    }

    @Test
    void testCollectWorkflowStatistics() {
        WorkflowEvent startEvent = WorkflowEvent.builder()
                .workflowId("wf_mon_1")
                .instanceId("inst_mon_1")
                .eventType("WORKFLOW_STARTED")
                .newState(WorkflowState.RUNNING)
                .build();

        WorkflowEvent completeEvent = WorkflowEvent.builder()
                .workflowId("wf_mon_1")
                .instanceId("inst_mon_1")
                .eventType("WORKFLOW_COMPLETED")
                .newState(WorkflowState.COMPLETED)
                .build();

        monitor.onWorkflowEvent(startEvent);
        monitor.onWorkflowEvent(completeEvent);

        ExecutionEvent unitEvent = ExecutionEvent.builder()
                .workflowId("wf_mon_1")
                .unitId("unit_1")
                .eventType("UNIT_EXECUTED")
                .build();

        monitor.onExecutionEvent(unitEvent);

        WorkflowStatistics stats = monitor.getStatistics();
        assertNotNull(stats);
        assertEquals(1, stats.getTotalWorkflowsExecuted());
        assertEquals(1, stats.getSuccessfulWorkflows());
        assertEquals(1, stats.getTotalUnitsExecuted());
        assertEquals(1.0, stats.getSuccessRate());
    }

    @Test
    void testEngineHealth() {
        assertTrue(monitor.isHealthy());
    }
}
