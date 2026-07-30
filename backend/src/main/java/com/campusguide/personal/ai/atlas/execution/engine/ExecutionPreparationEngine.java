package com.campusguide.personal.ai.atlas.execution.engine;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;
import com.campusguide.personal.ai.atlas.planning.context.PlanningContext;
import com.campusguide.personal.ai.atlas.planning.model.ExecutionPlan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Provider-independent Execution Preparation Engine.
 * Transforms deterministic ExecutionPlans into validated ExecutableWorkflows
 * without performing execution.
 */
@Slf4j
@Service("executionPreparationEngine")
public class ExecutionPreparationEngine {

    private final ExecutionPreparationPipeline pipeline;

    public ExecutionPreparationEngine(ExecutionPreparationPipeline pipeline) {
        this.pipeline = pipeline;
    }

    /**
     * Primary entry point transforming an ExecutionPlan into an ExecutableWorkflow.
     */
    public ExecutableWorkflow prepareWorkflow(ExecutionPlan plan) {
        if (plan == null) {
            log.warn("Null ExecutionPlan provided to ExecutionPreparationEngine");
            return ExecutableWorkflow.fallback("wf_" + UUID.randomUUID().toString().substring(0, 8), "Null ExecutionPlan provided");
        }
        ExecutionContext context = ExecutionContext.fromExecutionPlan(plan);
        return prepareWorkflow(context);
    }

    /**
     * Entry point transforming an ExecutionPlan and PlanningContext into an ExecutableWorkflow.
     */
    public ExecutableWorkflow prepareWorkflow(ExecutionPlan plan, PlanningContext planningContext) {
        if (plan == null) {
            log.warn("Null ExecutionPlan provided to ExecutionPreparationEngine");
            return ExecutableWorkflow.fallback("wf_" + UUID.randomUUID().toString().substring(0, 8), "Null ExecutionPlan provided");
        }
        ExecutionContext context = ExecutionContext.fromExecutionPlan(plan, planningContext);
        return prepareWorkflow(context);
    }

    /**
     * Core entry point transforming ExecutionContext into an ExecutableWorkflow.
     */
    public ExecutableWorkflow prepareWorkflow(ExecutionContext context) {
        if (context == null) {
            log.warn("Null ExecutionContext provided to ExecutionPreparationEngine");
            return ExecutableWorkflow.fallback("wf_" + UUID.randomUUID().toString().substring(0, 8), "Null ExecutionContext provided");
        }

        log.debug("ExecutionPreparationEngine preparing ExecutableWorkflow for contextId={}", context.getContextId());
        return pipeline.prepare(context);
    }
}
