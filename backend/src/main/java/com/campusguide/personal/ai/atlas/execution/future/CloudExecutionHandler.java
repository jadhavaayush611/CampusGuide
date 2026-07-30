package com.campusguide.personal.ai.atlas.execution.future;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;

/**
 * SPI interface supporting cloud runtime execution bindings (e.g. AWS Lambda, Cloudflare Workers, GCP).
 */
public interface CloudExecutionHandler {

    boolean supportsCloudExecution(ExecutionContext context, ExecutableWorkflow workflow);

    void prepareCloudDeployment(ExecutionContext context, ExecutableWorkflow workflow);
}
