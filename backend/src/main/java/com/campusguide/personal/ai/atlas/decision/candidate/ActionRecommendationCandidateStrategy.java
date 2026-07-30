package com.campusguide.personal.ai.atlas.decision.candidate;

import com.campusguide.personal.ai.atlas.decision.context.DecisionContext;
import com.campusguide.personal.ai.atlas.decision.model.DecisionCandidate;
import com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.engine.ReasoningEvidence;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Strategy that generates actionable recommendations (e.g. course registration, navigation, scheduling).
 */
@Component
public class ActionRecommendationCandidateStrategy implements CandidateStrategy {

    @Override
    public String getStrategyId() {
        return "ActionRecommendationCandidateStrategy";
    }

    @Override
    public int getOrder() {
        return 20;
    }

    @Override
    public boolean supports(DecisionContext context) {
        return context != null && context.getReasoningEvidence() != null
                && context.getReasoningEvidence().getCitedNodeNames() != null
                && !context.getReasoningEvidence().getCitedNodeNames().isEmpty();
    }

    @Override
    public List<DecisionCandidate> generateCandidates(DecisionContext context) {
        ReasoningEvidence evidence = context.getReasoningEvidence();
        double confidence = evidence != null ? evidence.getConfidence() : 0.5;

        Map<String, Object> params = new HashMap<>();
        params.put("targetEntity", evidence != null && !evidence.getCitedNodeNames().isEmpty() 
                ? evidence.getCitedNodeNames().get(0) : "system");
        params.put("actionName", "EXPLORE_DETAILS");

        DecisionCandidate candidate = DecisionCandidate.builder()
                .candidateId("cand_action_recommendation")
                .actionType("EXECUTE_ACTION")
                .description("Recommend interactive action for entity " + params.get("targetEntity"))
                .estimatedUtility(confidence * 0.85)
                .confidenceScore(confidence)
                .feasibilityScore(0.95)
                .rationale("Specific graph entities cited in reasoning evidence")
                .sourceStrategy(getStrategyId())
                .parameters(params)
                .build();

        return Collections.singletonList(candidate);
    }
}
