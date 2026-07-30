package com.campusguide.personal.ai.atlas.execution.runtime.events;

/**
 * Subscriber interface for handling runtime workflow and execution events.
 */
public interface EventSubscriber {

    default void onWorkflowEvent(WorkflowEvent event) {}
    default void onExecutionEvent(ExecutionEvent event) {}
}
