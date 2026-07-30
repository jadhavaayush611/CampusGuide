package com.campusguide.personal.ai.atlas.execution.explanation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Structured explanation of execution preparation rationale, approvals, risks,
 * rollback plans, required capabilities, and assumptions.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionExplanation implements Serializable {

    private static final long serialVersionUID = 1L;

    private String explanationId;
    private String summary;
    private String readinessRationale;

    @Builder.Default
    private List<ExecutionReason> reasons = new ArrayList<>();

    @Builder.Default
    private List<ExecutionEvidence> evidences = new ArrayList<>();

    @Builder.Default
    private List<String> assumptions = new ArrayList<>();

    @Builder.Default
    private List<String> requiredCapabilitiesSummary = new ArrayList<>();

    private String riskSummary;
    private String approvalSummary;
    private String rollbackSummary;

    public static ExecutionExplanation defaultExplanation(String summary) {
        return ExecutionExplanation.builder()
                .explanationId("expl_default")
                .summary(summary != null ? summary : "Workflow preparation explanation")
                .readinessRationale("Workflow validated and ready for execution runtime")
                .reasons(new ArrayList<>())
                .evidences(new ArrayList<>())
                .assumptions(new ArrayList<>())
                .requiredCapabilitiesSummary(new ArrayList<>())
                .riskSummary("Low execution risk")
                .approvalSummary("No special approvals required")
                .rollbackSummary("Automatic compensating rollback plan configured")
                .build();
    }
}
