package com.campusguide.personal.ai.atlas.execution.runtime.future;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;

/**
 * Extension point for serverless execution runtimes.
 */
public interface ServerlessRuntimeExtension extends RuntimeExtensionPoint {

    void executeServerless(ExecutionContext context, ExecutableWorkflow workflow);
}
