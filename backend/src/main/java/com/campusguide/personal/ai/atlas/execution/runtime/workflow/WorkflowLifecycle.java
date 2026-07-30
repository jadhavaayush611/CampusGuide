package com.campusguide.personal.ai.atlas.execution.runtime.workflow;

import com.campusguide.personal.ai.atlas.execution.model.ExecutionStage;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionUnit;
import com.campusguide.personal.ai.atlas.execution.runtime.tool.ToolResult;

/**
 * Interface defining lifecycle hooks for workflow runtime execution.
 */
public interface WorkflowLifecycle {

    default void onStart(WorkflowInstance instance) {}
    default void onStageStart(WorkflowInstance instance, ExecutionStage stage) {}
    default void onStageComplete(WorkflowInstance instance, ExecutionStage stage) {}
    default void onUnitStart(WorkflowInstance instance, ExecutionUnit unit) {}
    default void onUnitComplete(WorkflowInstance instance, ExecutionUnit unit, ToolResult result) {}
    default void onWait(WorkflowInstance instance, String reason) {}
    default void onResume(WorkflowInstance instance) {}
    default void onFailure(WorkflowInstance instance, String error) {}
    default void onRollback(WorkflowInstance instance, String reason) {}
    default void onCancel(WorkflowInstance instance, String reason) {}
    default void onComplete(WorkflowInstance instance) {}
}
