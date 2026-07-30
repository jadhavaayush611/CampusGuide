package com.campusguide.personal.ai.atlas.decision.candidate;

import com.campusguide.personal.ai.atlas.decision.context.DecisionContext;
import com.campusguide.personal.ai.atlas.decision.model.DecisionCandidate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Strategy that generates clarification candidates when confidence is ambiguous.
 */
@Component
public class ClarificationCandidateStrategy implements CandidateStrategy {

    @Override
    public String getStrategyId() {
        return "ClarificationCandidateStrategy";
    }

    @Override
    public int getOrder() {
        return 30;
    }

    @Override
    public boolean supports(DecisionContext context) {
        if (context == null) return false;
        double conf = context.getReasoningEvidence() != null ? context.getReasoningEvidence().getConfidence() : 0.0;
        return conf > 0.15 && conf < 0.60;
    }

    @Override
    public List<DecisionCandidate> generateCandidates(DecisionContext context) {
        Map<String, Object> params = new HashMap<>();
        params.put("promptText", "Could you clarify your request or narrow down the timeframe/department?");

        DecisionCandidate candidate = DecisionCandidate.builder()
                .candidateId("cand_clarification")
                .actionType("REQUEST_CLARIFICATION")
                .description("Ask user for intent clarification due to moderate reasoning confidence")
                .estimatedUtility(0.60)
                .confidenceScore(0.50)
                .feasibilityScore(1.0)
                .rationale("Reasoning confidence is in ambiguous range (0.15 - 0.60)")
                .sourceStrategy(getStrategyId())
                .parameters(params)
                .build();

        return Collections.singletonList(candidate);
    }
}
