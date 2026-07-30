package com.campusguide.personal.ai.atlas.orchestration.replanning;

import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;
import com.campusguide.personal.ai.atlas.planning.model.ExecutionPlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Replanning decision record output by DynamicReplanner.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplanningDecision {

    private String decisionId;
    private boolean replanRequired;
    private ReplanningTrigger trigger;
    private String reason;
    private ExecutionPlan updatedPlan;
    private ExecutableWorkflow updatedWorkflow;
    @Builder.Default
    private Instant timestamp = Instant.now();

    public static ReplanningDecision noReplan(String reason) {
        return ReplanningDecision.builder()
                .decisionId("replan_" + UUID.randomUUID().toString().substring(0, 8))
                .replanRequired(false)
                .reason(reason)
                .timestamp(Instant.now())
                .build();
    }

    public static ReplanningDecision replan(ReplanningTrigger trigger, String reason, ExecutionPlan updatedPlan, ExecutableWorkflow updatedWorkflow) {
        return ReplanningDecision.builder()
                .decisionId("replan_" + UUID.randomUUID().toString().substring(0, 8))
                .replanRequired(true)
                .trigger(trigger)
                .reason(reason)
                .updatedPlan(updatedPlan)
                .updatedWorkflow(updatedWorkflow)
                .timestamp(Instant.now())
                .build();
    }
}
