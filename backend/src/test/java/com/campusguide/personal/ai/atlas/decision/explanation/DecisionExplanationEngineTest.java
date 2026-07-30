package com.campusguide.personal.ai.atlas.decision.explanation;

import com.campusguide.personal.ai.atlas.decision.context.DecisionContext;
import com.campusguide.personal.ai.atlas.decision.model.DecisionCandidate;
import com.campusguide.personal.ai.atlas.decision.policy.PolicyComplianceResult;
import com.campusguide.personal.ai.atlas.decision.recommendation.Recommendation;
import com.campusguide.personal.ai.atlas.decision.recommendation.RecommendationBundle;
import com.campusguide.personal.ai.atlas.decision.recommendation.RecommendationType;
import com.campusguide.personal.ai.atlas.decision.utility.UtilityScore;
import com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.engine.ReasoningEvidence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DecisionExplanationEngineTest {

    private DecisionExplanationEngine explanationEngine;

    @BeforeEach
    void setUp() {
        explanationEngine = new DecisionExplanationEngine();
    }

    @Test
    @DisplayName("DecisionExplanationEngine synthesizes human-readable and structured rationale explaining why selected and why alternatives rejected")
    void testExplanationGeneration() {
        DecisionCandidate primaryCand = DecisionCandidate.simple("cand_primary", "DIRECT_ANSWER", "Primary answer", 0.90);
        Recommendation rec = Recommendation.builder()
                .candidate(primaryCand)
                .type(RecommendationType.PRIMARY)
                .rank(1)
                .utility(0.90)
                .build();

        RecommendationBundle bundle = RecommendationBundle.builder()
                .primaryRecommendation(rec)
                .rejectedReasons(Map.of("cand_denied", "Lacks admin permissions"))
                .overallRationale("Primary candidate selected due to high utility score 0.90")
                .build();

        ReasoningEvidence evidence = ReasoningEvidence.builder()
                .evidenceId("ev_1")
                .reasoningSummaryText("Graph traversal confirmed course CS101 availability")
                .confidence(0.90)
                .citedNodeNames(List.of("CS101"))
                .build();

        DecisionContext context = DecisionContext.fromReasoning(null, evidence);
        PolicyComplianceResult policyResult = PolicyComplianceResult.builder().appliedPolicies(List.of("PermissionPolicyRule")).build();

        UtilityScore uScore = UtilityScore.builder().candidateId("cand_primary").normalizedUtility(0.90).factorScores(Map.of("confidence", 0.90)).build();

        DecisionExplanation explanation = explanationEngine.generateExplanation(context, bundle, policyResult, Map.of(), Map.of("cand_primary", uScore));

        assertNotNull(explanation);
        assertNotNull(explanation.getSelectedCandidateReason());
        assertTrue(explanation.getAlternativeRejectionReasons().containsKey("cand_denied"));
        assertEquals(1, explanation.getSupportingEvidence().size());
        assertEquals("CS101", explanation.getSupportingEvidence().get(0).getCitedNodes().get(0));
        assertFalse(explanation.getDecisionReasons().isEmpty());
    }
}
