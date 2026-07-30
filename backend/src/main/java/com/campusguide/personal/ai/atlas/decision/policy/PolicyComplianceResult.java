package com.campusguide.personal.ai.atlas.decision.policy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Encapsulates full policy compliance details across all candidates evaluated.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyComplianceResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean fullyCompliant;
    
    @Builder.Default
    private List<String> appliedPolicies = Collections.emptyList();

    @Builder.Default
    private Map<String, List<PolicyEvaluationResult>> candidateEvaluations = new ConcurrentHashMap<>();

    public double getComplianceScore(String candidateId) {
        List<PolicyEvaluationResult> results = candidateEvaluations.get(candidateId);
        if (results == null || results.isEmpty()) return 1.0;
        boolean hasDenial = results.stream().anyMatch(r -> r.getStatus() == PolicyEvaluationResult.Status.DENIED);
        if (hasDenial) return 0.0;
        long total = results.size();
        long allowed = results.stream().filter(r -> r.getStatus() == PolicyEvaluationResult.Status.ALLOWED).count();
        return (double) allowed / total;
    }
}
