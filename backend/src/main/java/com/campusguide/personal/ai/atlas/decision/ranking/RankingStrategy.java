package com.campusguide.personal.ai.atlas.decision.ranking;

import com.campusguide.personal.ai.atlas.decision.evaluation.DecisionScore;
import com.campusguide.personal.ai.atlas.decision.model.DecisionCandidate;
import com.campusguide.personal.ai.atlas.decision.utility.UtilityScore;

import java.util.List;
import java.util.Map;

/**
 * Strategy interface for ranking candidates based on utility and evaluation scores.
 */
public interface RankingStrategy {

    String getStrategyName();

    DecisionRanking rankCandidates(List<DecisionCandidate> candidates, 
                                   Map<String, DecisionScore> scores, 
                                   Map<String, UtilityScore> utilities);
}
