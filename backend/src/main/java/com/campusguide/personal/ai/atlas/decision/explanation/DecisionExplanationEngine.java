package com.campusguide.personal.ai.atlas.decision.explanation;

import com.campusguide.personal.ai.atlas.decision.context.DecisionContext;
import com.campusguide.personal.ai.atlas.decision.evaluation.DecisionScore;
import com.campusguide.personal.ai.atlas.decision.model.DecisionCandidate;
import com.campusguide.personal.ai.atlas.decision.policy.PolicyComplianceResult;
import com.campusguide.personal.ai.atlas.decision.recommendation.Recommendation;
import com.campusguide.personal.ai.atlas.decision.recommendation.RecommendationBundle;
import com.campusguide.personal.ai.atlas.decision.utility.UtilityScore;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Engine synthesizing deterministic, explainable rationales for decisions and alternative rejections.
 */
@Component
public class DecisionExplanationEngine {

    public DecisionExplanation generateExplanation(DecisionContext context,
                                                    RecommendationBundle bundle,
                                                    PolicyComplianceResult policyCompliance,
                                                    Map<String, DecisionScore> scores,
                                                    Map<String, UtilityScore> utilities) {
        Recommendation primary = bundle != null ? bundle.getPrimaryRecommendation() : null;
        DecisionCandidate selected = primary != null ? primary.getCandidate() : null;

        String selectedReason = selected != null 
                ? "Candidate " + selected.getCandidateId() + " (" + selected.getActionType() + ") was selected with highest normalized utility (" + primary.getUtility() + ")."
                : "No candidate was selected.";

        Map<String, String> altRejections = new HashMap<>();
        if (bundle != null && bundle.getRejectedReasons() != null) {
            altRejections.putAll(bundle.getRejectedReasons());
        }

        if (bundle != null && bundle.getAlternativeRecommendations() != null) {
            for (Recommendation alt : bundle.getAlternativeRecommendations()) {
                altRejections.put(
                        alt.getCandidate().getCandidateId(),
                        "Ranked #" + alt.getRank() + " alternative with utility score " + alt.getUtility() + " (below primary candidate utility " + (primary != null ? primary.getUtility() : 0.0) + ")"
                );
            }
        }

        List<DecisionEvidence> evidenceList = new ArrayList<>();
        if (context != null && context.getReasoningEvidence() != null) {
            evidenceList.add(DecisionEvidence.fromReasoningEvidence(context.getReasoningEvidence()));
        }

        List<String> appliedPolicies = policyCompliance != null ? policyCompliance.getAppliedPolicies() : List.of();

        double confidence = selected != null ? selected.getConfidenceScore() : 0.0;
        String confidenceSummary = "Decision confidence score: " + String.format("%.2f", confidence);

        UtilityScore selectedUtility = (selected != null && utilities != null) ? utilities.get(selected.getCandidateId()) : null;
        Map<String, Double> utilityBreakdown = selectedUtility != null ? selectedUtility.getFactorScores() : Map.of();

        List<DecisionReason> reasons = new ArrayList<>();
        reasons.add(DecisionReason.decisive("HIGHEST_UTILITY", selectedReason));
        if (context != null && context.getObjective() != null) {
            reasons.add(DecisionReason.builder()
                    .reasonCode("OBJECTIVE_ALIGNMENT")
                    .narrative("Aligned with objective: " + context.getObjective().getPrimaryGoal())
                    .impactLevel(DecisionReason.ImpactLevel.HIGH)
                    .build());
        }

        return DecisionExplanation.builder()
                .primaryRationale(bundle != null ? bundle.getOverallRationale() : selectedReason)
                .selectedCandidateReason(selectedReason)
                .alternativeRejectionReasons(altRejections)
                .supportingEvidence(evidenceList)
                .appliedPolicies(appliedPolicies)
                .confidenceSummary(confidenceSummary)
                .utilityBreakdown(utilityBreakdown)
                .decisionReasons(reasons)
                .build();
    }
}
