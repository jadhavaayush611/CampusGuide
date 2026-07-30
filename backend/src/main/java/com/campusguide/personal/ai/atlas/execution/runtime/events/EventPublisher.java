package com.campusguide.personal.ai.atlas.execution.runtime.events;

/**
 * Interface for publishing runtime events.
 */
public interface EventPublisher {

    void publishWorkflowEvent(WorkflowEvent event);
    void publishExecutionEvent(ExecutionEvent event);
}
