package com.campusguide.personal.ai.atlas.execution.runtime.future;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;

/**
 * Extension point for containerized execution runtimes (K8s/Docker).
 */
public interface ContainerizedRuntimeExtension extends RuntimeExtensionPoint {

    void executeInContainer(ExecutionContext context, ExecutableWorkflow workflow);
}
