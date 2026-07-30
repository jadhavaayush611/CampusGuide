package com.campusguide.personal.ai.atlas.decision.model;

import com.campusguide.personal.ai.atlas.decision.context.DecisionContext;
import com.campusguide.personal.ai.atlas.decision.context.DecisionObjective;
import com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.engine.ReasoningEvidence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DecisionModelTest {

    @Test
    @DisplayName("Decision model encapsulates objectives, candidates, selected action, confidence, and metadata")
    void testDecisionModelEncapsulation() {
        DecisionCandidate candidate = DecisionCandidate.simple("cand_1", "DIRECT_ANSWER", "Test Candidate", 0.95);
        DecisionMetadata metadata = DecisionMetadata.createDefault("trace_123");
        DecisionObjective objective = DecisionObjective.defaultObjective("academic_query");

        Decision decision = Decision.builder()
                .decisionId("dec_1")
                .objective(objective)
                .candidates(List.of(candidate))
                .selectedCandidate(candidate)
                .confidence(0.95)
                .rationale("High confidence candidate selected")
                .metadata(metadata)
                .build();

        assertEquals("dec_1", decision.getDecisionId());
        assertEquals("academic_query", decision.getObjective().getIntent());
        assertEquals(1, decision.getCandidates().size());
        assertEquals(candidate, decision.getSelectedCandidate());
        assertEquals(0.95, decision.getConfidence());
        assertEquals("trace_123", decision.getMetadata().getTraceId());
    }

    @Test
    @DisplayName("DecisionOutcome creates graceful fallback on low confidence or error")
    void testDecisionOutcomeFallback() {
        DecisionOutcome fallbackOutcome = DecisionOutcome.fallback("out_1", "Low confidence fallback");

        assertNotNull(fallbackOutcome);
        assertEquals("out_1", fallbackOutcome.getOutcomeId());
        assertEquals(DecisionStatus.DEGRADED, fallbackOutcome.getStatus());
        assertNotNull(fallbackOutcome.getSelectedAction());
        assertEquals("FALLBACK_RESPONSE", fallbackOutcome.getSelectedAction().getActionType());
        assertTrue((Boolean) fallbackOutcome.getExecutionHints().get("isFallback"));
    }

    @Test
    @DisplayName("DecisionContext initializes from ReasoningEvidence")
    void testDecisionContextFromReasoning() {
        ReasoningEvidence evidence = ReasoningEvidence.builder()
                .evidenceId("ev_1")
                .objectiveDescription("Test graph reasoning")
                .confidence(0.85)
                .reasoningSummaryText("Summary narrative")
                .citedNodeNames(List.of("CS101"))
                .build();

        DecisionContext context = DecisionContext.fromReasoning(null, evidence);

        assertNotNull(context);
        assertNotNull(context.getContextId());
        assertEquals(evidence, context.getReasoningEvidence());
        assertEquals(0.85, context.getReasoningEvidence().getConfidence());
        assertEquals("Test graph reasoning", context.getObjective().getIntent());
    }
}
