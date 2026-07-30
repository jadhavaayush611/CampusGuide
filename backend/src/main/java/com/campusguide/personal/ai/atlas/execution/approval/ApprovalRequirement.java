package com.campusguide.personal.ai.atlas.execution.approval;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates determined approval requirement levels for a workflow.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalRequirement implements Serializable {

    private static final long serialVersionUID = 1L;

    private String requirementId;
    private String workflowId;

    @Builder.Default
    private boolean approvalRequired = false;

    @Builder.Default
    private ApprovalLevel requiredApprovalLevel = ApprovalLevel.NONE;

    @Builder.Default
    private List<String> approvalReasons = new ArrayList<>();

    @Builder.Default
    private List<String> approverRoles = new ArrayList<>();

    @Builder.Default
    private boolean autoApprovable = true;

    public enum ApprovalLevel {
        NONE,
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL_ADMIN
    }

    public static ApprovalRequirement none(String workflowId) {
        return ApprovalRequirement.builder()
                .requirementId("app_req_none")
                .workflowId(workflowId)
                .approvalRequired(false)
                .requiredApprovalLevel(ApprovalLevel.NONE)
                .autoApprovable(true)
                .build();
    }
}
