package com.campusguide.personal.ai.atlas.decision.ranking;

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
 * Result of ranking candidate decisions deterministically.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DecisionRanking implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private List<DecisionCandidate> sortedCandidates = Collections.emptyList();

    private DecisionCandidate topCandidate;

    @Builder.Default
    private Map<String, Integer> candidateRanks = new ConcurrentHashMap<>();

    @Builder.Default
    private List<String> tieBreakingLog = Collections.emptyList();
}
