package com.campusguide.personal.ai.atlas.execution.future;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;

/**
 * SPI interface supporting in-process local workflow execution bindings.
 */
public interface LocalExecutionHandler {

    boolean supportsLocalExecution(ExecutionContext context, ExecutableWorkflow workflow);

    void prepareLocalEnvironment(ExecutionContext context, ExecutableWorkflow workflow);
}
