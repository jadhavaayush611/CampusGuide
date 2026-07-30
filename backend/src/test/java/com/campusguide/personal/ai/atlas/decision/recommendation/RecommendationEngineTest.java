package com.campusguide.personal.ai.atlas.decision.recommendation;

import com.campusguide.personal.ai.atlas.decision.model.DecisionCandidate;
import com.campusguide.personal.ai.atlas.decision.policy.PolicyComplianceResult;
import com.campusguide.personal.ai.atlas.decision.policy.PolicyEvaluationResult;
import com.campusguide.personal.ai.atlas.decision.ranking.DecisionRanking;
import com.campusguide.personal.ai.atlas.decision.utility.UtilityScore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RecommendationEngineTest {

    private RecommendationEngine recommendationEngine;

    @BeforeEach
    void setUp() {
        recommendationEngine = new RecommendationEngine();
    }

    @Test
    @DisplayName("RecommendationEngine builds primary and alternative recommendations and isolates policy rejected candidates")
    void testRecommendationBundleBuilding() {
        DecisionCandidate cPrimary = DecisionCandidate.simple("cand_primary", "DIRECT_ANSWER", "Primary candidate", 0.95);
        DecisionCandidate cAlt = DecisionCandidate.simple("cand_alt", "EXECUTE_ACTION", "Alternative candidate", 0.80);
        DecisionCandidate cRejected = DecisionCandidate.simple("cand_rejected", "DENIED_ACTION", "Denied candidate", 0.70);

        DecisionRanking ranking = DecisionRanking.builder()
                .sortedCandidates(List.of(cPrimary, cAlt, cRejected))
                .topCandidate(cPrimary)
                .build();

        PolicyComplianceResult policyCompliance = PolicyComplianceResult.builder()
                .fullyCompliant(false)
                .appliedPolicies(List.of("PermissionPolicyRule"))
                .candidateEvaluations(Map.of(
                        "cand_primary", List.of(PolicyEvaluationResult.allow("p1", "perm")),
                        "cand_alt", List.of(PolicyEvaluationResult.allow("p1", "perm")),
                        "cand_rejected", List.of(PolicyEvaluationResult.deny("p1", "perm", "Permission denied"))
                ))
                .build();

        Map<String, UtilityScore> utilities = Map.of(
                "cand_primary", UtilityScore.builder().candidateId("cand_primary").normalizedUtility(0.95).build(),
                "cand_alt", UtilityScore.builder().candidateId("cand_alt").normalizedUtility(0.80).build(),
                "cand_rejected", UtilityScore.builder().candidateId("cand_rejected").normalizedUtility(0.70).build()
        );

        RecommendationBundle bundle = recommendationEngine.buildBundle(ranking, policyCompliance, utilities);

        assertNotNull(bundle);
        assertNotNull(bundle.getPrimaryRecommendation());
        assertEquals("cand_primary", bundle.getPrimaryRecommendation().getCandidate().getCandidateId());

        assertEquals(1, bundle.getAlternativeRecommendations().size());
        assertEquals("cand_alt", bundle.getAlternativeRecommendations().get(0).getCandidate().getCandidateId());

        assertEquals(1, bundle.getRejectedCandidates().size());
        assertEquals("cand_rejected", bundle.getRejectedCandidates().get(0).getCandidateId());
        assertTrue(bundle.getRejectedReasons().containsKey("cand_rejected"));
    }
}
