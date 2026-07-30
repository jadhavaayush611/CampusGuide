package com.campusguide.personal.ai.atlas.execution.runtime.future;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;

/**
 * Extension point for distributed node execution runtimes.
 */
public interface DistributedRuntimeExtension extends RuntimeExtensionPoint {

    void executeDistributed(ExecutionContext context, ExecutableWorkflow workflow);
}
