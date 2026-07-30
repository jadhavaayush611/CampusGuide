package com.campusguide.personal.ai.atlas.execution.runtime.future;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;

/**
 * Common extension point interface for future runtime environments.
 */
public interface RuntimeExtensionPoint {

    String getExtensionName();

    boolean isSupported(ExecutionContext context, ExecutableWorkflow workflow);
}
