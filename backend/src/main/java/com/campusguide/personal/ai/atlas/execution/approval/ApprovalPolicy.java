package com.campusguide.personal.ai.atlas.execution.approval;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Policy definition for required approvals.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalPolicy implements Serializable {

    private static final long serialVersionUID = 1L;

    private String policyId;
    private String policyName;

    @Builder.Default
    private double minRiskScoreForApproval = 0.7;

    @Builder.Default
    private boolean requiresHumanApproval = false;

    @Builder.Default
    private Set<String> restrictedActions = new HashSet<>();

    @Builder.Default
    private String approvalRoleRequired = "ADMIN";

    public static ApprovalPolicy defaultPolicy() {
        Set<String> restricted = new HashSet<>();
        restricted.add("MUTATION");
        restricted.add("DELETE");
        restricted.add("EXTERNAL_SYSTEM_WRITE");

        return ApprovalPolicy.builder()
                .policyId("policy_default")
                .policyName("Default Execution Approval Policy")
                .minRiskScoreForApproval(0.7)
                .requiresHumanApproval(false)
                .restrictedActions(restricted)
                .approvalRoleRequired("ROLE_ADMIN")
                .build();
    }
}
