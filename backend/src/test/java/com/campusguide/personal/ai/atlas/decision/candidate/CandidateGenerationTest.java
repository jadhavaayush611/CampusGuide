package com.campusguide.personal.ai.atlas.decision.candidate;

import com.campusguide.personal.ai.atlas.decision.context.DecisionContext;

import com.campusguide.personal.ai.atlas.decision.model.DecisionCandidate;
import com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.engine.ReasoningEvidence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CandidateGenerationTest {

    private CandidateRegistry registry;
    private DecisionCandidateGenerator generator;

    @BeforeEach
    void setUp() {
        DirectAnswerCandidateStrategy directStrategy = new DirectAnswerCandidateStrategy();
        ActionRecommendationCandidateStrategy actionStrategy = new ActionRecommendationCandidateStrategy();
        ClarificationCandidateStrategy clarificationStrategy = new ClarificationCandidateStrategy();
        FallbackCandidateStrategy fallbackStrategy = new FallbackCandidateStrategy();

        registry = new CandidateRegistry(List.of(directStrategy, actionStrategy, clarificationStrategy, fallbackStrategy));
        generator = new DecisionCandidateGenerator(registry);
    }

    @Test
    @DisplayName("CandidateGenerator generates multiple candidate decisions using pluggable strategies")
    void testCandidateGeneration() {
        ReasoningEvidence evidence = ReasoningEvidence.builder()
                .evidenceId("ev_high_conf")
                .objectiveDescription("Find course CS101")
                .confidence(0.90)
                .reasoningSummaryText("CS101 is available in Fall semester.")
                .citedNodeNames(List.of("CS101"))
                .citedRelationshipTypes(List.of("OFFERED_IN"))
                .build();

        DecisionContext context = DecisionContext.fromReasoning(null, evidence);

        List<DecisionCandidate> candidates = generator.generateCandidates(context);

        assertNotNull(candidates);
        assertFalse(candidates.isEmpty());
        assertTrue(candidates.stream().anyMatch(c -> "DIRECT_ANSWER".equals(c.getActionType())));
        assertTrue(candidates.stream().anyMatch(c -> "EXECUTE_ACTION".equals(c.getActionType())));
        assertTrue(candidates.stream().anyMatch(c -> "FALLBACK_RESPONSE".equals(c.getActionType())));
    }

    @Test
    @DisplayName("Clarification strategy triggers for ambiguous reasoning confidence")
    void testClarificationStrategyForAmbiguousConfidence() {
        ReasoningEvidence ambiguousEvidence = ReasoningEvidence.builder()
                .evidenceId("ev_ambiguous")
                .objectiveDescription("Find course details")
                .confidence(0.40)
                .reasoningSummaryText("Multiple matching courses found")
                .citedNodeNames(List.of("CS101", "CS102"))
                .build();

        DecisionContext context = DecisionContext.fromReasoning(null, ambiguousEvidence);
        List<DecisionCandidate> candidates = generator.generateCandidates(context);

        assertTrue(candidates.stream().anyMatch(c -> "REQUEST_CLARIFICATION".equals(c.getActionType())));
    }
}
