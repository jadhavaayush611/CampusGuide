package com.campusguide.personal.ai.atlas.orchestration.replanning;

import com.campusguide.personal.ai.atlas.execution.engine.ExecutionPreparationEngine;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;
import com.campusguide.personal.ai.atlas.planning.context.PlanningContext;
import com.campusguide.personal.ai.atlas.planning.engine.PlanningEngine;
import com.campusguide.personal.ai.atlas.planning.model.ExecutionPlan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service for dynamic workflow replanning upon execution failures, missing capabilities, environmental changes, or constraint changes.
 * Integrates directly with existing Planning Engine and Execution Preparation Engine.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicReplanner {

    private final PlanningEngine planningEngine;
    private final ExecutionPreparationEngine executionPreparationEngine;

    public ReplanningDecision evaluateAndReplan(ReplanningTrigger trigger, String reason, PlanningContext context, ReplanningPolicy policy) {
        if (trigger == null) {
            log.warn("Null trigger provided for dynamic replanning evaluation");
            return ReplanningDecision.noReplan("Trigger is null");
        }

        ReplanningPolicy effectivePolicy = policy != null ? policy : ReplanningPolicy.defaultPolicy();
        if (!effectivePolicy.getEnabledTriggers().contains(trigger)) {
            log.info("Trigger {} is disabled under active ReplanningPolicy", trigger);
            return ReplanningDecision.noReplan("Trigger " + trigger + " is disabled by policy");
        }

        log.info("DynamicReplanner evaluating replanning for trigger {} (Reason: {})", trigger, reason);

        PlanningContext effectiveContext = context != null ? context : PlanningContext.builder()
                .contextId("ctx_" + UUID.randomUUID().toString().substring(0, 8))
                .build();

        try {
            // Re-invoke Planning Engine to generate updated ExecutionPlan
            ExecutionPlan newPlan = planningEngine.generatePlan(effectiveContext);
            if (newPlan == null) {
                log.error("PlanningEngine returned null plan during replanning");
                return ReplanningDecision.noReplan("Planning engine failed to produce plan");
            }

            // Re-invoke Execution Preparation Engine to prepare new ExecutableWorkflow
            ExecutableWorkflow newWorkflow = executionPreparationEngine.prepareWorkflow(newPlan);
            if (newWorkflow == null) {
                log.error("ExecutionPreparationEngine returned null workflow during replanning");
                return ReplanningDecision.noReplan("Execution preparation engine failed to produce workflow");
            }

            log.info("DynamicReplanner successfully generated replacement workflow {} for plan {}",
                    newWorkflow.getWorkflowId(), newPlan.getPlanId());

            return ReplanningDecision.replan(trigger, reason, newPlan, newWorkflow);
        } catch (Exception e) {
            log.error("Error during dynamic replanning: {}", e.getMessage(), e);
            return ReplanningDecision.noReplan("Replanning failed: " + e.getMessage());
        }
    }
}
