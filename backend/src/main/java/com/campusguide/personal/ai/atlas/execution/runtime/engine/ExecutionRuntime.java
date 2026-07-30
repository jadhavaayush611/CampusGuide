package com.campusguide.personal.ai.atlas.execution.runtime.engine;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;
import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowInstance;

/**
 * Core interface for Atlas's provider-independent Execution Runtime Engine.
 *
 * STRICT INVARIANT:
 * Consumes ONLY ExecutionContext and ExecutableWorkflow.
 * Must NEVER inspect ReasoningContext, DecisionContext, PlanningContext, or ExecutionPlan directly.
 */
public interface ExecutionRuntime {

    WorkflowInstance executeWorkflow(ExecutionContext context, ExecutableWorkflow workflow);
}
