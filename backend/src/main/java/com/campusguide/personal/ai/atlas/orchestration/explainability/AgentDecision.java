package com.campusguide.personal.ai.atlas.orchestration.explainability;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Explanation model capturing an autonomous agent decision.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentDecision {

    private String agentId;
    private String decisionType;
    private String rationale;
    @Builder.Default
    private Instant timestamp = Instant.now();
    @Builder.Default
    private Map<String, Object> attributes = new HashMap<>();
}
