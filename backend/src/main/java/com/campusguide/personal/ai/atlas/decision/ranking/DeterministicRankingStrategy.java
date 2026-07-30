package com.campusguide.personal.ai.atlas.decision.ranking;

import com.campusguide.personal.ai.atlas.decision.evaluation.DecisionScore;
import com.campusguide.personal.ai.atlas.decision.model.DecisionCandidate;
import com.campusguide.personal.ai.atlas.decision.utility.UtilityScore;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Deterministic ranking strategy with explicit tie-breaking:
 * 1. Normalized Utility Score
 * 2. Reasoning Confidence Score
 * 3. Lexicographical CandidateId tie-breaking
 */
@Component
public class DeterministicRankingStrategy implements RankingStrategy {

    @Override
    public String getStrategyName() {
        return "DeterministicRankingStrategy";
    }

    @Override
    public DecisionRanking rankCandidates(List<DecisionCandidate> candidates, 
                                           Map<String, DecisionScore> scores, 
                                           Map<String, UtilityScore> utilities) {
        if (candidates == null || candidates.isEmpty()) {
            return DecisionRanking.builder().build();
        }

        List<DecisionCandidate> sorted = new ArrayList<>(candidates);
        List<String> tieBreaks = new ArrayList<>();

        sorted.sort((c1, c2) -> {
            UtilityScore u1 = utilities != null ? utilities.get(c1.getCandidateId()) : null;
            UtilityScore u2 = utilities != null ? utilities.get(c2.getCandidateId()) : null;

            double util1 = u1 != null ? u1.getNormalizedUtility() : c1.getEstimatedUtility();
            double util2 = u2 != null ? u2.getNormalizedUtility() : c2.getEstimatedUtility();

            int cmpUtil = Double.compare(util2, util1); // descending
            if (cmpUtil != 0) return cmpUtil;

            // Secondary: confidence
            double conf1 = c1.getConfidenceScore();
            double conf2 = c2.getConfidenceScore();
            int cmpConf = Double.compare(conf2, conf1); // descending
            if (cmpConf != 0) {
                tieBreaks.add("Tie broken by confidence score between " + c1.getCandidateId() + " and " + c2.getCandidateId());
                return cmpConf;
            }

            // Final tie-breaker: CandidateId lexicographical
            tieBreaks.add("Tie broken lexicographically by candidateId between " + c1.getCandidateId() + " and " + c2.getCandidateId());
            return c1.getCandidateId().compareTo(c2.getCandidateId());
        });

        Map<String, Integer> rankMap = new HashMap<>();
        for (int i = 0; i < sorted.size(); i++) {
            rankMap.put(sorted.get(i).getCandidateId(), i + 1);
        }

        return DecisionRanking.builder()
                .sortedCandidates(sorted)
                .topCandidate(sorted.isEmpty() ? null : sorted.get(0))
                .candidateRanks(rankMap)
                .tieBreakingLog(tieBreaks)
                .build();
    }
}
