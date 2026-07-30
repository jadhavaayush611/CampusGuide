package com.campusguide.personal.ai.atlas.execution.runtime.monitoring;

import com.campusguide.personal.ai.atlas.execution.runtime.events.EventSubscriber;
import com.campusguide.personal.ai.atlas.execution.runtime.events.ExecutionEvent;
import com.campusguide.personal.ai.atlas.execution.runtime.events.WorkflowEvent;
import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowInstance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Monitors execution, collects metrics, and maintains workflow statistics.
 * Strictly guarantees privacy by omitting sensitive payload data from logs and metrics.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExecutionMonitor implements EventSubscriber {

    private final RuntimeMetrics runtimeMetrics;
    private final HealthMonitor healthMonitor;

    private final AtomicLong totalWorkflows = new AtomicLong(0);
    private final AtomicLong successfulWorkflows = new AtomicLong(0);
    private final AtomicLong failedWorkflows = new AtomicLong(0);
    private final AtomicLong cancelledWorkflows = new AtomicLong(0);
    private final AtomicLong totalUnitsExecuted = new AtomicLong(0);
    private final AtomicLong totalRetries = new AtomicLong(0);
    private final AtomicLong totalRollbacks = new AtomicLong(0);

    @Override
    public void onWorkflowEvent(WorkflowEvent event) {
        if (event == null) return;

        if ("WORKFLOW_STARTED".equals(event.getEventType())) {
            totalWorkflows.incrementAndGet();
        } else if ("WORKFLOW_COMPLETED".equals(event.getEventType())) {
            successfulWorkflows.incrementAndGet();
            runtimeMetrics.recordWorkflowExecution("COMPLETED", 0L);
        } else if ("WORKFLOW_FAILED".equals(event.getEventType())) {
            failedWorkflows.incrementAndGet();
            runtimeMetrics.recordWorkflowExecution("FAILED", 0L);
        } else if ("WORKFLOW_CANCELLED".equals(event.getEventType())) {
            cancelledWorkflows.incrementAndGet();
            runtimeMetrics.recordWorkflowExecution("CANCELLED", 0L);
        } else if ("WORKFLOW_ROLLBACK".equals(event.getEventType())) {
            totalRollbacks.incrementAndGet();
            runtimeMetrics.recordRollback(event.getWorkflowId());
        }
    }

    @Override
    public void onExecutionEvent(ExecutionEvent event) {
        if (event == null) return;

        if ("UNIT_EXECUTED".equals(event.getEventType())) {
            totalUnitsExecuted.incrementAndGet();
            runtimeMetrics.recordUnitExecution((String) event.getDetails().get("capability"), "SUCCESS", 0L);
        } else if ("UNIT_RETRY".equals(event.getEventType())) {
            totalRetries.incrementAndGet();
            runtimeMetrics.recordRetry(event.getUnitId());
        }
    }

    public WorkflowStatistics getStatistics() {
        return WorkflowStatistics.builder()
                .totalWorkflowsExecuted(totalWorkflows.get())
                .successfulWorkflows(successfulWorkflows.get())
                .failedWorkflows(failedWorkflows.get())
                .cancelledWorkflows(cancelledWorkflows.get())
                .totalUnitsExecuted(totalUnitsExecuted.get())
                .totalRetries(totalRetries.get())
                .totalRollbacks(totalRollbacks.get())
                .build();
    }

    public boolean isHealthy() {
        return healthMonitor.isEngineHealthy();
    }
}
