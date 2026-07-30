package com.campusguide.personal.ai.atlas.decision.ranking;

import com.campusguide.personal.ai.atlas.decision.evaluation.DecisionScore;
import com.campusguide.personal.ai.atlas.decision.model.DecisionCandidate;
import com.campusguide.personal.ai.atlas.decision.utility.UtilityScore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Component executing decision candidate ranking.
 */
@Slf4j
@Component
public class DecisionRanker {

    private final RankingStrategy rankingStrategy;

    public DecisionRanker(RankingStrategy rankingStrategy) {
        this.rankingStrategy = rankingStrategy;
    }

    public DecisionRanking rankCandidates(List<DecisionCandidate> candidates, 
                                           Map<String, DecisionScore> scores, 
                                           Map<String, UtilityScore> utilities) {
        DecisionRanking ranking = rankingStrategy.rankCandidates(candidates, scores, utilities);
        log.debug("Ranked {} candidates. Top candidate: {}", 
                candidates != null ? candidates.size() : 0, 
                ranking.getTopCandidate() != null ? ranking.getTopCandidate().getCandidateId() : "none");
        return ranking;
    }
}
