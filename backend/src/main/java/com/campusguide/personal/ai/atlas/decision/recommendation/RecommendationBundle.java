package com.campusguide.personal.ai.atlas.decision.recommendation;

import com.campusguide.personal.ai.atlas.decision.model.DecisionCandidate;
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
 * Bundle containing primary recommendation, alternative recommendations, rejected candidates, and rationales.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationBundle implements Serializable {

    private static final long serialVersionUID = 1L;

    private Recommendation primaryRecommendation;

    @Builder.Default
    private List<Recommendation> alternativeRecommendations = Collections.emptyList();

    @Builder.Default
    private List<DecisionCandidate> rejectedCandidates = Collections.emptyList();

    @Builder.Default
    private Map<String, String> rejectedReasons = new ConcurrentHashMap<>();

    private String overallRationale;
}
