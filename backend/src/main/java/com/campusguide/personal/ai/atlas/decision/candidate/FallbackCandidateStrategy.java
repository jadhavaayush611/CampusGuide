package com.campusguide.personal.ai.atlas.decision.candidate;

import com.campusguide.personal.ai.atlas.decision.context.DecisionContext;
import com.campusguide.personal.ai.atlas.decision.model.DecisionCandidate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fallback candidate strategy ensuring at least one candidate option is always available.
 */
@Component
public class FallbackCandidateStrategy implements CandidateStrategy {

    @Override
    public String getStrategyId() {
        return "FallbackCandidateStrategy";
    }

    @Override
    public int getOrder() {
        return 999;
    }

    @Override
    public boolean supports(DecisionContext context) {
        return true; // Always supports as safety catch
    }

    @Override
    public List<DecisionCandidate> generateCandidates(DecisionContext context) {
        Map<String, Object> params = new HashMap<>();
        params.put("fallbackMessage", "No specific graph decision candidate met high confidence threshold.");

        DecisionCandidate candidate = DecisionCandidate.builder()
                .candidateId("cand_fallback")
                .actionType("FALLBACK_RESPONSE")
                .description("Default graceful fallback decision response")
                .estimatedUtility(0.20)
                .confidenceScore(0.20)
                .feasibilityScore(1.0)
                .rationale("Guaranteed baseline candidate strategy")
                .sourceStrategy(getStrategyId())
                .parameters(params)
                .build();

        return Collections.singletonList(candidate);
    }
}
