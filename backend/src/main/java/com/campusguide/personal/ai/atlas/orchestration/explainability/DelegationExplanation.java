package com.campusguide.personal.ai.atlas.orchestration.explainability;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Explanation model detailing task delegation choices and candidate agent scoring.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DelegationExplanation {

    private String taskAssignmentId;
    private String taskId;
    private String selectedAgentId;
    private String strategyUsed;
    private String justification;
    @Builder.Default
    private Map<String, Double> candidateAgentScores = new HashMap<>();
}
