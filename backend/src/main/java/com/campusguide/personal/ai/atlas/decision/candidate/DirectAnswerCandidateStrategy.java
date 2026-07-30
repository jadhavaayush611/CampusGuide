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
 * Strategy that generates direct textual response candidates based on ReasoningEvidence.
 */
@Component
public class DirectAnswerCandidateStrategy implements CandidateStrategy {

    @Override
    public String getStrategyId() {
        return "DirectAnswerCandidateStrategy";
    }

    @Override
    public int getOrder() {
        return 10;
    }

    @Override
    public boolean supports(DecisionContext context) {
        return context != null && context.getReasoningEvidence() != null 
                && context.getReasoningEvidence().getConfidence() >= 0.3;
    }

    @Override
    public List<DecisionCandidate> generateCandidates(DecisionContext context) {
        ReasoningEvidence evidence = context.getReasoningEvidence();
        double confidence = evidence != null ? evidence.getConfidence() : 0.5;

        Map<String, Object> params = new HashMap<>();
        params.put("summaryText", evidence != null ? evidence.getReasoningSummaryText() : "Direct response available.");
        params.put("citedNodes", evidence != null ? evidence.getCitedNodeNames() : Collections.emptyList());

        DecisionCandidate candidate = DecisionCandidate.builder()
                .candidateId("cand_direct_answer")
                .actionType("DIRECT_ANSWER")
                .description("Provide direct synthesized response based on graph evidence")
                .estimatedUtility(confidence * 0.9)
                .confidenceScore(confidence)
                .feasibilityScore(1.0)
                .rationale("High confidence evidence found in graph context")
                .sourceStrategy(getStrategyId())
                .parameters(params)
                .build();

        return Collections.singletonList(candidate);
    }
}
