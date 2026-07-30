package com.campusguide.personal.ai.atlas.execution.explanation;

import com.campusguide.personal.ai.atlas.execution.approval.ApprovalRequirement;
import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionStage;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionUnit;
import com.campusguide.personal.ai.atlas.execution.risk.ExecutionRisk;
import com.campusguide.personal.ai.atlas.execution.rollback.RollbackPlan;
import com.campusguide.personal.ai.atlas.execution.validation.ValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ExecutionExplanationEngine synthesizes structured explanations covering approvals,
 * risks, rollback plans, required capabilities, assumptions, and readiness.
 */
@Slf4j
@Component
public class ExecutionExplanationEngine {

    public ExecutionExplanation explain(ExecutionContext context,
                                         ExecutableWorkflow workflow,
                                         ValidationResult validationResult,
                                         ExecutionRisk risk,
                                         ApprovalRequirement approvalRequirement,
                                         RollbackPlan rollbackPlan) {

        log.debug("Synthesizing execution explanation for workflowId={}", workflow != null ? workflow.getWorkflowId() : "unknown");

        List<ExecutionReason> reasons = new ArrayList<>();
        List<ExecutionEvidence> evidences = new ArrayList<>();
        List<String> assumptions = new ArrayList<>();
        List<String> requiredCapabilitiesSummary = new ArrayList<>();

        // Extract capabilities
        if (workflow != null && workflow.getStages() != null) {
            requiredCapabilitiesSummary = workflow.getStages().stream()
                    .flatMap(s -> s.getExecutionUnits().stream())
                    .map(ExecutionUnit::getTargetCapability)
                    .filter(c -> c != null && !c.isBlank())
                    .distinct()
                    .collect(Collectors.toList());
        }

        // Add Validation Reason
        boolean isV = validationResult != null && validationResult.isValid();
        reasons.add(ExecutionReason.builder()
                .reasonId("reason_val")
                .category(ReasonCategory.READINESS)
                .title("Validation Readiness")
                .description(isV ? "All workflow validation rules satisfied clean" : "Validation violations found: " + validationResult.getViolations())
                .impactScore(isV ? 1.0 : 0.0)
                .build());

        // Add Approval Reason
        if (approvalRequirement != null && approvalRequirement.isApprovalRequired()) {
            reasons.add(ExecutionReason.builder()
                    .reasonId("reason_app")
                    .category(ReasonCategory.APPROVAL)
                    .title("Approval Requirement: " + approvalRequirement.getRequiredApprovalLevel())
                    .description("Approval required due to: " + String.join(", ", approvalRequirement.getApprovalReasons()))
                    .impactScore(0.8)
                    .build());
        }

        // Add Risk Reason
        if (risk != null) {
            reasons.add(ExecutionReason.builder()
                    .reasonId("reason_risk")
                    .category(ReasonCategory.RISK)
                    .title("Execution Risk Assessment: " + risk.getRiskCategory())
                    .description("Composite risk score: " + String.format("%.2f", risk.getOverallRiskScore()))
                    .impactScore(risk.getOverallRiskScore())
                    .build());
        }

        // Add Rollback Reason
        if (rollbackPlan != null) {
            reasons.add(ExecutionReason.builder()
                    .reasonId("reason_rb")
                    .category(ReasonCategory.ROLLBACK)
                    .title("Deterministic Rollback Strategy: " + rollbackPlan.getRecoveryStrategy())
                    .description("Rollback plan configured with " + rollbackPlan.getRollbackUnits().size() + " compensating units")
                    .impactScore(0.5)
                    .build());
        }

        // Add Assumptions
        assumptions.add("Target execution environment supports registered tool capabilities");
        assumptions.add("User authentication & security context remain valid during runtime execution");

        // Synthesize summary strings
        String readinessRationale = isV ? "Workflow is fully prepared, validated, and ready for runtime execution." :
                "Workflow preparation incomplete due to validation errors.";

        String riskSummary = risk != null ?
                "Risk Level: " + risk.getRiskCategory() + " (Score: " + String.format("%.2f", risk.getOverallRiskScore()) + ")" :
                "Risk level unassessed";

        String approvalSummary = approvalRequirement != null && approvalRequirement.isApprovalRequired() ?
                "Requires " + approvalRequirement.getRequiredApprovalLevel() + " approval from " + approvalRequirement.getApproverRoles() :
                "No approval required for execution.";

        String rollbackSummary = rollbackPlan != null ?
                "Rollback Strategy: " + rollbackPlan.getRecoveryStrategy() + " (" + rollbackPlan.getRollbackUnits().size() + " steps)" :
                "No rollback plan configured.";

        return ExecutionExplanation.builder()
                .explanationId("expl_" + UUID.randomUUID().toString().substring(0, 8))
                .summary("Executable workflow successfully assembled and evaluated for " + (workflow != null ? workflow.getWorkflowId() : "workflow"))
                .readinessRationale(readinessRationale)
                .reasons(reasons)
                .evidences(evidences)
                .assumptions(assumptions)
                .requiredCapabilitiesSummary(requiredCapabilitiesSummary)
                .riskSummary(riskSummary)
                .approvalSummary(approvalSummary)
                .rollbackSummary(rollbackSummary)
                .build();
    }
}
