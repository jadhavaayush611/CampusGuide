package com.campusguide.personal.ai.atlas.execution.future;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;

/**
 * SPI interface supporting external workflow orchestrators (e.g. Temporal, Camunda, Airflow).
 */
public interface WorkflowOrchestrationHandler {

    boolean supportsOrchestrator(String orchestratorType);

    void exportToOrchestrator(ExecutionContext context, ExecutableWorkflow workflow, String orchestratorType);
}
