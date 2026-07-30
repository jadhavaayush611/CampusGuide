package com.campusguide.personal.ai.atlas.decision.utility;

import com.campusguide.personal.ai.atlas.decision.context.DecisionContext;
import com.campusguide.personal.ai.atlas.decision.context.DecisionObjective;
import com.campusguide.personal.ai.atlas.decision.evaluation.DecisionScore;
import com.campusguide.personal.ai.atlas.decision.model.DecisionCandidate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UtilityCalculatorTest {

    private UtilityCalculator utilityCalculator;

    @BeforeEach
    void setUp() {
        utilityCalculator = new UtilityCalculator();
    }

    @Test
    @DisplayName("UtilityCalculator computes normalized utility score between 0.0 and 1.0")
    void testUtilityCalculation() {
        DecisionCandidate candidate = DecisionCandidate.builder()
                .candidateId("cand_1")
                .actionType("DIRECT_ANSWER")
                .confidenceScore(0.85)
                .feasibilityScore(0.90)
                .estimatedUtility(0.80)
                .build();

        DecisionContext context = DecisionContext.builder()
                .objective(DecisionObjective.builder().priority(DecisionObjective.Priority.HIGH).build())
                .build();

        DecisionScore score = DecisionScore.builder()
                .candidateId("cand_1")
                .policyComplianceScore(1.0)
                .build();

        UtilityScore utilityScore = utilityCalculator.calculateUtility(candidate, context, score);

        assertNotNull(utilityScore);
        assertEquals("cand_1", utilityScore.getCandidateId());
        assertTrue(utilityScore.getNormalizedUtility() >= 0.0 && utilityScore.getNormalizedUtility() <= 1.0);
        assertTrue(utilityScore.getFactorScores().containsKey("confidence"));
        assertTrue(utilityScore.getFactorScores().containsKey("relevance"));
        assertTrue(utilityScore.getFactorScores().containsKey("urgency"));
    }
}
