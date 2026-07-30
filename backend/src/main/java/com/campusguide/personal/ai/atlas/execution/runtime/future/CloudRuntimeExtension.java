package com.campusguide.personal.ai.atlas.execution.runtime.future;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;

/**
 * Extension point for cloud execution runtimes.
 */
public interface CloudRuntimeExtension extends RuntimeExtensionPoint {

    void executeInCloud(ExecutionContext context, ExecutableWorkflow workflow);
}
