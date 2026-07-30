package com.campusguide.personal.ai.atlas.decision.recommendation;

import com.campusguide.personal.ai.atlas.decision.model.DecisionCandidate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Individual recommendation wrapping a decision candidate.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recommendation implements Serializable {

    private static final long serialVersionUID = 1L;

    private DecisionCandidate candidate;
    private RecommendationType type;
    private int rank;
    private double utility;
    private String rationale;

    @Builder.Default
    private Map<String, Object> executionPayload = new ConcurrentHashMap<>();
}
