package com.campusguide.personal.ai.atlas.context;

import com.campusguide.personal.ai.atlas.context.evidence.EvidenceBundle;
import com.campusguide.personal.ai.atlas.context.evidence.EvidenceScore;
import com.campusguide.personal.ai.atlas.context.evidence.RetrievalEvidence;
import com.campusguide.personal.ai.atlas.context.prioritization.ContextPrioritizer;
import com.campusguide.personal.ai.atlas.context.prioritization.PrioritizationDecision;
import com.campusguide.personal.ai.atlas.context.query.QueryContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ContextPrioritizerTest {

    @Test
    @DisplayName("ContextPrioritizer ranks domain context bundles using multi-criteria quality composite")
    void testContextPrioritizer_MultiCriteriaRanking() {
        ContextPrioritizer prioritizer = new ContextPrioritizer();
        AtlasContext context = new AtlasContext("conv-1", "user-1");

        // High quality academic bundle
        RetrievalEvidence ev1 = RetrievalEvidence.builder()
                .contentSnippet("CS 101 Course Details")
                .score(EvidenceScore.builder().relevanceScore(0.95).confidenceScore(0.95).sourceAuthorityScore(0.9).freshnessScore(0.9).qualityScore(0.9).build())
                .build();
        ev1.getScore().calculateOverallScore();

        EvidenceBundle academicBundle = EvidenceBundle.builder()
                .targetDomain("academic")
                .evidences(List.of(ev1))
                .confidence(0.95)
                .build();
        academicBundle.recalculateAggregateScore();
        context.addEvidenceBundle(academicBundle);

        // Low quality campus bundle
        RetrievalEvidence ev2 = RetrievalEvidence.builder()
                .contentSnippet("General notice")
                .score(EvidenceScore.builder().relevanceScore(0.3).confidenceScore(0.4).sourceAuthorityScore(0.5).freshnessScore(0.3).qualityScore(0.4).build())
                .build();
        ev2.getScore().calculateOverallScore();

        EvidenceBundle campusBundle = EvidenceBundle.builder()
                .targetDomain("campus")
                .evidences(List.of(ev2))
                .confidence(0.4)
                .build();
        campusBundle.recalculateAggregateScore();
        context.addEvidenceBundle(campusBundle);

        QueryContext q = QueryContext.builder().rawQuery("Tell me about CS 101").build();

        List<PrioritizationDecision> decisions = prioritizer.prioritize(context, q, 1);

        assertNotNull(decisions);
        assertEquals(2, decisions.size());

        // Academic bundle should rank #1 and be kept
        PrioritizationDecision rank1 = decisions.get(0);
        assertEquals("academic", rank1.getTargetDomain());
        assertEquals(1, rank1.getRank());
        assertTrue(rank1.isKept());

        // Campus bundle should rank #2 and be pruned due to maxTopEntries limit = 1
        PrioritizationDecision rank2 = decisions.get(1);
        assertEquals("campus", rank2.getTargetDomain());
        assertEquals(2, rank2.getRank());
        assertFalse(rank2.isKept());
    }
}
