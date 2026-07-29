package com.campusguide.personal.ai.atlas.context;

import com.campusguide.personal.ai.atlas.context.evidence.EvidenceBundle;
import com.campusguide.personal.ai.atlas.context.evidence.EvidenceScore;
import com.campusguide.personal.ai.atlas.context.evidence.EvidenceSource;
import com.campusguide.personal.ai.atlas.context.evidence.EvidenceType;
import com.campusguide.personal.ai.atlas.context.evidence.RetrievalEvidence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EvidenceModelTest {

    @Test
    @DisplayName("EvidenceScore calculates weighted overall score accurately")
    void testEvidenceScore_CalculateOverallScore() {
        EvidenceScore score = EvidenceScore.builder()
                .relevanceScore(0.8)
                .confidenceScore(0.9)
                .sourceAuthorityScore(1.0)
                .freshnessScore(0.7)
                .qualityScore(0.8)
                .build();

        double expected = (0.8 * 0.30) + (0.9 * 0.25) + (1.0 * 0.20) + (0.7 * 0.15) + (0.8 * 0.10);
        double actual = score.calculateOverallScore();

        assertEquals(expected, actual, 0.001);
        assertEquals(expected, score.getOverallScore(), 0.001);
    }

    @Test
    @DisplayName("RetrievalEvidence holds structured evidence metadata correctly")
    void testRetrievalEvidence_Structure() {
        RetrievalEvidence evidence = RetrievalEvidence.builder()
                .type(EvidenceType.CAMPUS_KNOWLEDGE)
                .source(EvidenceSource.KNOWLEDGE_BASE)
                .entityKey("building:CSH")
                .contentSnippet("Turing Computer Science Hall")
                .rationale("Matches query entity")
                .build();

        assertNotNull(evidence.getId());
        assertEquals(EvidenceType.CAMPUS_KNOWLEDGE, evidence.getType());
        assertEquals(EvidenceSource.KNOWLEDGE_BASE, evidence.getSource());
        assertEquals("building:CSH", evidence.getEntityKey());
        assertTrue(evidence.getTimestamp() > 0);
    }

    @Test
    @DisplayName("EvidenceBundle aggregates evidence scores correctly")
    void testEvidenceBundle_Aggregation() {
        EvidenceBundle bundle = EvidenceBundle.builder()
                .targetDomain("campus")
                .build();

        RetrievalEvidence e1 = RetrievalEvidence.builder()
                .type(EvidenceType.CAMPUS_KNOWLEDGE)
                .source(EvidenceSource.CAMPUS_SERVICE)
                .score(EvidenceScore.builder().relevanceScore(0.9).confidenceScore(0.8).sourceAuthorityScore(0.9).freshnessScore(0.8).qualityScore(0.8).build())
                .build();

        RetrievalEvidence e2 = RetrievalEvidence.builder()
                .type(EvidenceType.DIRECT)
                .source(EvidenceSource.DATABASE)
                .score(EvidenceScore.builder().relevanceScore(0.7).confidenceScore(0.6).sourceAuthorityScore(1.0).freshnessScore(0.6).qualityScore(0.6).build())
                .build();

        bundle.addEvidence(e1);
        bundle.addEvidence(e2);

        assertEquals(2, bundle.getEvidences().size());
        assertNotNull(bundle.getAggregateScore());
        assertEquals(0.8, bundle.getAggregateScore().getRelevanceScore(), 0.001);
        assertEquals(0.7, bundle.getAggregateScore().getConfidenceScore(), 0.001);
        assertTrue(bundle.getConfidence() > 0);
    }
}
