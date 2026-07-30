package com.campusguide.personal.ai.atlas.orchestration.explainability;

import com.campusguide.personal.ai.atlas.orchestration.delegation.TaskAssignment;
import com.campusguide.personal.ai.atlas.orchestration.replanning.ReplanningDecision;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service for generating structured explanations for multi-agent delegation, coordination, replanning, and supervisor interventions.
 */
@Slf4j
@Service
public class OrchestrationExplanationEngine {

    public DelegationExplanation explainDelegation(TaskAssignment assignment, String strategyUsed) {
        if (assignment == null) {
            return DelegationExplanation.builder()
                    .taskAssignmentId("unknown")
                    .justification("No task assignment provided")
                    .build();
        }

        return DelegationExplanation.builder()
                .taskAssignmentId(assignment.getAssignmentId())
                .taskId(assignment.getTaskId())
                .selectedAgentId(assignment.getAgentId())
                .strategyUsed(strategyUsed != null ? strategyUsed : "HYBRID")
                .justification(assignment.getExplanation() != null ? assignment.getExplanation() : "Task assigned based on capability and load")
                .build();
    }

    public CoordinationReason explainCoordination(String barrierId, String description) {
        return CoordinationReason.builder()
                .reasonId("coord_" + UUID.randomUUID().toString().substring(0, 8))
                .coordinationType("BARRIER_SYNCHRONIZATION")
                .description(description != null ? description : "Synchronization barrier " + barrierId + " enforced")
                .build();
    }

    public String explainReplanning(ReplanningDecision decision) {
        if (decision == null || !decision.isReplanRequired()) {
            return "No replanning was required or executed.";
        }
        return String.format("Dynamic replanning triggered by %s. Reason: %s. Replacement workflow ID: %s.",
                decision.getTrigger(), decision.getReason(),
                decision.getUpdatedWorkflow() != null ? decision.getUpdatedWorkflow().getWorkflowId() : "N/A");
    }

    public String explainSupervisorIntervention(String targetAgentId, String action, String reason) {
        return String.format("Supervisor intervention on agent %s: Action '%s' executed due to: %s.",
                targetAgentId, action, reason);
    }
}
