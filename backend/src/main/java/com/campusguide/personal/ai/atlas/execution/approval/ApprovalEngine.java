package com.campusguide.personal.ai.atlas.execution.approval;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionStage;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionUnit;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionUnitType;
import com.campusguide.personal.ai.atlas.execution.risk.ExecutionRisk;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * ApprovalEngine determines required approval levels without performing approvals.
 */
@Slf4j
@Component
public class ApprovalEngine {

    public ApprovalRequirement evaluateApprovals(ExecutionContext context, ExecutableWorkflow workflow, ExecutionRisk risk) {
        log.debug("Evaluating approval requirements for workflowId={}", workflow != null ? workflow.getWorkflowId() : "unknown");

        String workflowId = workflow != null ? workflow.getWorkflowId() : "wf_unknown";
        List<String> reasons = new ArrayList<>();
        List<String> approverRoles = new ArrayList<>();
        ApprovalRequirement.ApprovalLevel level = ApprovalRequirement.ApprovalLevel.NONE;
        boolean approvalRequired = false;

        // Rule 1: High risk score triggers approval
        if (risk != null && risk.getOverallRiskScore() >= 0.7) {
            approvalRequired = true;
            level = ApprovalRequirement.ApprovalLevel.HIGH;
            reasons.add("High execution risk score (" + risk.getOverallRiskScore() + ") exceeds threshold 0.7");
            approverRoles.add("ROLE_ADMIN");
        } else if (risk != null && risk.getOverallRiskScore() >= 0.4) {
            approvalRequired = true;
            level = ApprovalRequirement.ApprovalLevel.MEDIUM;
            reasons.add("Medium execution risk score (" + risk.getOverallRiskScore() + ")");
            approverRoles.add("ROLE_SUPERVISOR");
        }

        // Rule 2: Restricted mutation actions trigger approval
        if (workflow != null && workflow.getStages() != null) {
            for (ExecutionStage stage : workflow.getStages()) {
                if (stage.getExecutionUnits() != null) {
                    for (ExecutionUnit unit : stage.getExecutionUnits()) {
                        if (unit.getUnitType() == ExecutionUnitType.MUTATION || unit.isApprovalRequired()) {
                            approvalRequired = true;
                            if (level.ordinal() < ApprovalRequirement.ApprovalLevel.MEDIUM.ordinal()) {
                                level = ApprovalRequirement.ApprovalLevel.MEDIUM;
                            }
                            reasons.add("Mutation/action unit requires approval: " + unit.getTitle());
                            if (!approverRoles.contains("ROLE_USER")) {
                                approverRoles.add("ROLE_USER");
                            }
                        }
                    }
                }
            }
        }

        // Rule 3: Explicit constraint requirement
        if (context != null && context.getConstraints() != null && context.getConstraints().isRequireExplicitApproval()) {
            approvalRequired = true;
            if (level.ordinal() < ApprovalRequirement.ApprovalLevel.HIGH.ordinal()) {
                level = ApprovalRequirement.ApprovalLevel.HIGH;
            }
            reasons.add("ExecutionConstraints explicitly enforce mandatory approval");
            if (!approverRoles.contains("ROLE_ADMIN")) {
                approverRoles.add("ROLE_ADMIN");
            }
        }

        return ApprovalRequirement.builder()
                .requirementId("app_req_" + workflowId)
                .workflowId(workflowId)
                .approvalRequired(approvalRequired)
                .requiredApprovalLevel(level)
                .approvalReasons(reasons)
                .approverRoles(approverRoles)
                .autoApprovable(!approvalRequired)
                .build();
    }
}
