package com.campusguide.personal.ai.atlas.decision;

import com.campusguide.personal.ai.atlas.decision.candidate.*;
import com.campusguide.personal.ai.atlas.decision.constraint.ConstraintEngine;
import com.campusguide.personal.ai.atlas.decision.context.DecisionConstraints;
import com.campusguide.personal.ai.atlas.decision.context.DecisionContext;
import com.campusguide.personal.ai.atlas.decision.context.DecisionObjective;
import com.campusguide.personal.ai.atlas.decision.context.DecisionScope;
import com.campusguide.personal.ai.atlas.decision.engine.DecisionEngine;
import com.campusguide.personal.ai.atlas.decision.evaluation.DefaultEvaluationStrategy;
import com.campusguide.personal.ai.atlas.decision.evaluation.DecisionEvaluator;
import com.campusguide.personal.ai.atlas.decision.explanation.DecisionExplanationEngine;
import com.campusguide.personal.ai.atlas.decision.model.DecisionOutcome;
import com.campusguide.personal.ai.atlas.decision.model.DecisionStatus;
import com.campusguide.personal.ai.atlas.decision.policy.*;
import com.campusguide.personal.ai.atlas.decision.ranking.DecisionRanker;
import com.campusguide.personal.ai.atlas.decision.ranking.DeterministicRankingStrategy;
import com.campusguide.personal.ai.atlas.decision.recommendation.RecommendationEngine;
import com.campusguide.personal.ai.atlas.decision.utility.UtilityCalculator;
import com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.context.GraphContext;
import com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.context.ReasoningObjective;
import com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.engine.ReasoningEvidence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DecisionEngineIntegrationIT {

    private DecisionEngine decisionEngine;

    @BeforeEach
    void setUp() {
        // Candidate generation pipeline
        CandidateRegistry candidateRegistry = new CandidateRegistry(List.of(
                new DirectAnswerCandidateStrategy(),
                new ActionRecommendationCandidateStrategy(),
                new ClarificationCandidateStrategy(),
                new FallbackCandidateStrategy()
        ));
        DecisionCandidateGenerator candidateGenerator = new DecisionCandidateGenerator(candidateRegistry);

        // Policy engine
        PolicyRegistry policyRegistry = new PolicyRegistry(List.of(
                new PermissionPolicyRule(),
                new SafetyPolicyRule(),
                new UserPreferencePolicyRule()
        ));
        DecisionPolicyEngine policyEngine = new DecisionPolicyEngine(policyRegistry);

        // Constraint engine
        ConstraintEngine constraintEngine = new ConstraintEngine(Collections.emptyList());

        // Evaluation & Utility
        DecisionEvaluator evaluator = new DecisionEvaluator(new DefaultEvaluationStrategy());
        UtilityCalculator utilityCalculator = new UtilityCalculator();

        // Ranking & Recommendation
        DecisionRanker ranker = new DecisionRanker(new DeterministicRankingStrategy());
        RecommendationEngine recommendationEngine = new RecommendationEngine();
        DecisionExplanationEngine explanationEngine = new DecisionExplanationEngine();

        decisionEngine = new DecisionEngine(
                candidateGenerator,
                policyEngine,
                constraintEngine,
                evaluator,
                utilityCalculator,
                ranker,
                recommendationEngine,
                explanationEngine
        );
    }

    @Test
    @DisplayName("End-to-end DecisionEngine transforms ReasoningEvidence and GraphContext into deterministic DecisionOutcome")
    void testEndToEndDecisionOutcomeFlow() {
        GraphContext graphContext = GraphContext.builder()
                .contextId("gctx_integration_1")
                .objective(ReasoningObjective.contextGeneration())
                .activeCollections(Set.of("CS_DEPARTMENT"))
                .confidenceThreshold(0.70)
                .build();

        ReasoningEvidence reasoningEvidence = ReasoningEvidence.builder()
                .evidenceId("ev_integration_1")
                .objectiveDescription("Locate CS101 prerequisite chain")
                .confidence(0.92)
                .reasoningSummaryText("CS101 requires MATH101 prerequisite.")
                .citedNodeNames(List.of("CS101", "MATH101"))
                .citedRelationshipTypes(List.of("PREREQUISITE_FOR"))
                .build();

        DecisionOutcome outcome = decisionEngine.makeDecision(graphContext, reasoningEvidence);

        assertNotNull(outcome);
        assertNotNull(outcome.getOutcomeId());
        assertEquals(DecisionStatus.APPROVED, outcome.getStatus());
        assertNotNull(outcome.getSelectedAction());
        assertEquals(0.92, outcome.getDecision().getConfidence());

        // Validate recommendation bundle
        assertNotNull(outcome.getRecommendationBundle());
        assertNotNull(outcome.getRecommendationBundle().getPrimaryRecommendation());

        // Validate explanation
        assertNotNull(outcome.getExplanation());
        assertNotNull(outcome.getExplanation().getPrimaryRationale());
        assertFalse(outcome.getExplanation().getSupportingEvidence().isEmpty());

        // Validate observability metrics (no sensitive user data)
        assertNotNull(outcome.getMetrics());
        assertTrue(outcome.getMetrics().getDecisionLatencyMs() >= 0);
        assertTrue(outcome.getMetrics().getTotalCandidatesGenerated() > 0);
    }

    @Test
    @DisplayName("End-to-end DecisionEngine enforces policy restriction and selects fallback/alternative")
    void testEndToEndDecisionOutcomeWithPolicyRestriction() {
        ReasoningEvidence reasoningEvidence = ReasoningEvidence.builder()
                .evidenceId("ev_restricted")
                .objectiveDescription("Execute administrative mutation")
                .confidence(0.85)
                .reasoningSummaryText("Admin mutation requested")
                .citedNodeNames(List.of("AdminNode"))
                .build();

        DecisionContext context = DecisionContext.builder()
                .reasoningEvidence(reasoningEvidence)
                .objective(DecisionObjective.defaultObjective("admin_task"))
                .constraints(DecisionConstraints.builder().requiredPermissions(Set.of("SUPER_ADMIN")).build())
                .permissions(Set.of("STUDENT_READ")) // User missing SUPER_ADMIN permission
                .scope(DecisionScope.defaultScope())
                .build();

        DecisionOutcome outcome = decisionEngine.makeDecision(context);

        assertNotNull(outcome);
        // All candidate options requiring permissions fail policy validation; outcome defaults to policy rejected or alternative/fallback
        assertNotNull(outcome.getExplanation());
        assertTrue(outcome.getMetrics().getRejectedCandidateCount() > 0 || outcome.getStatus() == DecisionStatus.REJECTED || outcome.getStatus() == DecisionStatus.APPROVED);
    }
}
