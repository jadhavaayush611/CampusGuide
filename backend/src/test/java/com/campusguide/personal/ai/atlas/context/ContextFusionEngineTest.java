package com.campusguide.personal.ai.atlas.context;

import com.campusguide.personal.ai.atlas.context.evidence.EvidenceBundle;
import com.campusguide.personal.ai.atlas.context.evidence.EvidenceScore;
import com.campusguide.personal.ai.atlas.context.evidence.EvidenceSource;
import com.campusguide.personal.ai.atlas.context.evidence.EvidenceType;
import com.campusguide.personal.ai.atlas.context.evidence.RetrievalEvidence;
import com.campusguide.personal.ai.atlas.context.fusion.ConflictResolution;
import com.campusguide.personal.ai.atlas.context.fusion.ConflictResolver;
import com.campusguide.personal.ai.atlas.context.fusion.ContextFusionEngine;
import com.campusguide.personal.ai.atlas.context.fusion.ContextFusionEngine.ContextFusionResult;
import com.campusguide.personal.ai.atlas.context.fusion.ContextMerger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ContextFusionEngineTest {

    private ContextMerger merger;
    private ConflictResolver resolver;
    private ContextFusionEngine fusionEngine;

    @BeforeEach
    void setUp() {
        merger = new ContextMerger();
        resolver = new ConflictResolver();
        fusionEngine = new ContextFusionEngine(merger, resolver);
    }

    @Test
    @DisplayName("ContextMerger deduplicates identical snippets while preserving unique evidence")
    void testContextMerger_Deduplication() {
        RetrievalEvidence e1 = RetrievalEvidence.builder()
                .contentSnippet("Building CSH hours: 7am-10pm")
                .score(EvidenceScore.builder().relevanceScore(0.8).build())
                .build();
        e1.getScore().calculateOverallScore();

        RetrievalEvidence e2 = RetrievalEvidence.builder()
                .contentSnippet("Building CSH hours: 7am-10pm")
                .score(EvidenceScore.builder().relevanceScore(0.8).build())
                .build();

        RetrievalEvidence e3 = RetrievalEvidence.builder()
                .contentSnippet("Building CSH has VR lab")
                .score(EvidenceScore.builder().relevanceScore(0.9).build())
                .build();
        e3.getScore().calculateOverallScore();

        EvidenceBundle b1 = EvidenceBundle.builder().targetDomain("campus").evidences(List.of(e1)).build();
        EvidenceBundle b2 = EvidenceBundle.builder().targetDomain("campus").evidences(List.of(e2, e3)).build();

        EvidenceBundle merged = merger.mergeBundles(List.of(b1, b2), "campus");

        assertNotNull(merged);
        assertEquals(2, merged.getEvidences().size());
        assertEquals("Building CSH has VR lab", merged.getEvidences().get(0).getContentSnippet());
    }

    @Test
    @DisplayName("ConflictResolver resolves conflicts favoring higher source priority and freshness")
    void testConflictResolver_FreshnessAndSourcePriority() {
        long now = System.currentTimeMillis();

        RetrievalEvidence oldDbEvidence = RetrievalEvidence.builder()
                .entityKey("prof_smith_office")
                .source(EvidenceSource.DATABASE)
                .contentSnippet("Room 304")
                .timestamp(now - 86400000L * 7) // 7 days old
                .score(EvidenceScore.builder().relevanceScore(0.8).freshnessScore(0.2).confidenceScore(0.8).sourceAuthorityScore(1.0).qualityScore(0.8).build())
                .build();

        RetrievalEvidence freshServiceEvidence = RetrievalEvidence.builder()
                .entityKey("prof_smith_office")
                .source(EvidenceSource.CAMPUS_SERVICE)
                .contentSnippet("Room 412 (Moved)")
                .timestamp(now) // brand new
                .score(EvidenceScore.builder().relevanceScore(0.95).freshnessScore(1.0).confidenceScore(0.95).sourceAuthorityScore(0.90).qualityScore(0.95).build())
                .build();

        ConflictResolution resolution = resolver.resolve(oldDbEvidence, freshServiceEvidence);

        assertNotNull(resolution);
        assertEquals("Room 412 (Moved)", resolution.getWinningValue());
        assertEquals(EvidenceSource.CAMPUS_SERVICE, resolution.getWinningSource());
        assertTrue(resolution.getWinningScore() > resolution.getLosingScore());
    }

    @Test
    @DisplayName("ContextFusionEngine orchestrates fusion and conflict resolution across domain bundles")
    void testContextFusionEngine_FullFusion() {
        AtlasContext context = new AtlasContext("conv-1", "user-1");

        RetrievalEvidence e1 = RetrievalEvidence.builder()
                .entityKey("dept_cs")
                .type(EvidenceType.DOMAIN_SERVICE)
                .source(EvidenceSource.ACADEMIC_SERVICE)
                .contentSnippet("Computer Science Dept")
                .score(EvidenceScore.builder().relevanceScore(0.9).confidenceScore(0.9).sourceAuthorityScore(0.85).qualityScore(0.9).build())
                .build();
        e1.getScore().calculateOverallScore();

        EvidenceBundle b1 = EvidenceBundle.builder()
                .targetDomain("academic")
                .evidences(List.of(e1))
                .build();

        ContextFusionResult result = fusionEngine.fuse(context, List.of(b1));

        assertNotNull(result);
        assertEquals(1, result.fusedBundles().size());
        assertTrue(result.fusedBundles().containsKey("academic"));
        assertFalse(result.fusionDecisions().isEmpty());
    }
}
