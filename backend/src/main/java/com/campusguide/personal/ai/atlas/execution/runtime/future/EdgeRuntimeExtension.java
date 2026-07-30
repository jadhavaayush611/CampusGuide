package com.campusguide.personal.ai.atlas.execution.runtime.future;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;

/**
 * Extension point for edge runtime execution (Cloudflare Workers, Edge nodes).
 */
public interface EdgeRuntimeExtension extends RuntimeExtensionPoint {

    void executeOnEdge(ExecutionContext context, ExecutableWorkflow workflow);
}
