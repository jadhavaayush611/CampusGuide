package com.campusguide.personal.ai.atlas.decision.recommendation;

import com.campusguide.personal.ai.atlas.decision.model.DecisionCandidate;
import com.campusguide.personal.ai.atlas.decision.policy.PolicyComplianceResult;
import com.campusguide.personal.ai.atlas.decision.policy.PolicyEvaluationResult;
import com.campusguide.personal.ai.atlas.decision.ranking.DecisionRanking;
import com.campusguide.personal.ai.atlas.decision.utility.UtilityScore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Recommendation Engine creating RecommendationBundle from ranked candidates.
 */
@Slf4j
@Component("atlasDecisionRecommendationEngine")
public class RecommendationEngine {

    public RecommendationBundle buildBundle(DecisionRanking ranking, 
                                             PolicyComplianceResult policyCompliance, 
                                             Map<String, UtilityScore> utilities) {
        if (ranking == null || ranking.getSortedCandidates().isEmpty()) {
            log.warn("Empty ranking provided to RecommendationEngine");
            return RecommendationBundle.builder()
                    .overallRationale("No viable decision candidate available.")
                    .build();
        }

        List<DecisionCandidate> sorted = ranking.getSortedCandidates();
        List<Recommendation> allowedRecs = new ArrayList<>();
        List<DecisionCandidate> rejectedCands = new ArrayList<>();
        Map<String, String> rejectedReasons = new HashMap<>();

        for (int i = 0; i < sorted.size(); i++) {
            DecisionCandidate cand = sorted.get(i);
            boolean isDenied = false;
            String denialReason = "Policy non-compliant";

            if (policyCompliance != null) {
                List<PolicyEvaluationResult> evals = policyCompliance.getCandidateEvaluations().get(cand.getCandidateId());
                if (evals != null) {
                    for (PolicyEvaluationResult eval : evals) {
                        if (eval.getStatus() == PolicyEvaluationResult.Status.DENIED) {
                            isDenied = true;
                            denialReason = eval.getRejectionReason() != null ? eval.getRejectionReason() : eval.getRuleName();
                            break;
                        }
                    }
                }
            }

            if (isDenied) {
                rejectedCands.add(cand);
                rejectedReasons.put(cand.getCandidateId(), denialReason);
            } else {
                UtilityScore uScore = utilities != null ? utilities.get(cand.getCandidateId()) : null;
                double util = uScore != null ? uScore.getNormalizedUtility() : cand.getEstimatedUtility();

                RecommendationType type = allowedRecs.isEmpty() ? RecommendationType.PRIMARY : RecommendationType.ALTERNATIVE;
                Recommendation rec = Recommendation.builder()
                        .candidate(cand)
                        .type(type)
                        .rank(allowedRecs.size() + 1)
                        .utility(util)
                        .rationale(cand.getRationale() != null ? cand.getRationale() : "Rank " + (allowedRecs.size() + 1) + " candidate")
                        .executionPayload(cand.getParameters())
                        .build();

                allowedRecs.add(rec);
            }
        }

        Recommendation primary = allowedRecs.isEmpty() ? null : allowedRecs.get(0);
        List<Recommendation> alternatives = allowedRecs.size() > 1 
                ? allowedRecs.subList(1, allowedRecs.size()) : new ArrayList<>();

        String overallRationale = primary != null 
                ? "Selected " + primary.getCandidate().getActionType() + " with utility score " + primary.getUtility()
                : "All candidates were rejected by active policies.";

        return RecommendationBundle.builder()
                .primaryRecommendation(primary)
                .alternativeRecommendations(alternatives)
                .rejectedCandidates(rejectedCands)
                .rejectedReasons(rejectedReasons)
                .overallRationale(overallRationale)
                .build();
    }
}
