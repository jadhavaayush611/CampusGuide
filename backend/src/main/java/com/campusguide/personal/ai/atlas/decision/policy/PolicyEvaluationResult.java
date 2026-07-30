package com.campusguide.personal.ai.atlas.decision.policy;

import com.campusguide.personal.ai.atlas.decision.model.DecisionCandidate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Result of evaluating a single policy rule against a candidate decision.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyEvaluationResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private String ruleId;
    private String ruleName;
    private Status status;
    private String rejectionReason;
    private DecisionCandidate modifiedCandidate;

    public enum Status {
        ALLOWED,
        DENIED,
        MODIFIED
    }

    public static PolicyEvaluationResult allow(String ruleId, String ruleName) {
        return PolicyEvaluationResult.builder()
                .ruleId(ruleId)
                .ruleName(ruleName)
                .status(Status.ALLOWED)
                .build();
    }

    public static PolicyEvaluationResult deny(String ruleId, String ruleName, String reason) {
        return PolicyEvaluationResult.builder()
                .ruleId(ruleId)
                .ruleName(ruleName)
                .status(Status.DENIED)
                .rejectionReason(reason)
                .build();
    }
}
