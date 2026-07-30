package com.campusguide.personal.ai.atlas.execution.future;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;

/**
 * SPI interface supporting distributed node workflow execution bindings.
 */
public interface DistributedExecutionHandler {

    boolean supportsDistributedExecution(ExecutionContext context, ExecutableWorkflow workflow);

    void prepareDistributedNodes(ExecutionContext context, ExecutableWorkflow workflow);
}
