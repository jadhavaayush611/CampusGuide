package com.campusguide.personal.ai.atlas.knowledge.ranking;

import com.campusguide.personal.ai.atlas.context.query.QueryContext;
import com.campusguide.personal.ai.atlas.context.query.QueryIntent;
import com.campusguide.personal.ai.atlas.knowledge.artifact.ArtifactIdentifier;
import com.campusguide.personal.ai.atlas.knowledge.artifact.ArtifactMetadata;
import com.campusguide.personal.ai.atlas.knowledge.artifact.ArtifactSource;
import com.campusguide.personal.ai.atlas.knowledge.artifact.KnowledgeArtifact;
import com.campusguide.personal.ai.atlas.knowledge.collection.KnowledgeCollectionRegistry;
import com.campusguide.personal.ai.atlas.knowledge.retrieval.hybrid.HybridRankingEngine.HybridCandidate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArtifactRankingServiceTest {

    private KnowledgeCollectionRegistry registry;
    private ArtifactRankingService rankingService;

    @BeforeEach
    void setUp() {
        registry = new KnowledgeCollectionRegistry();
        rankingService = new ArtifactRankingService(registry);
    }

    @Test
    @DisplayName("Should evaluate 7 dimensions and rank artifacts deterministically")
    void testArtifactRankingDeterministicOrdering() {
        KnowledgeArtifact artA = KnowledgeArtifact.builder()
                .id(ArtifactIdentifier.of("art_A"))
                .content("Computer science graduation requirements document.")
                .collectionId(KnowledgeCollectionRegistry.ACADEMIC_CATALOG)
                .metadata(ArtifactMetadata.builder().category("academic").domain("academic").build())
                .source(ArtifactSource.builder().title("Academic Catalog 2026").sourceType("pdf").build())
                .build();

        KnowledgeArtifact artB = KnowledgeArtifact.builder()
                .id(ArtifactIdentifier.of("art_B"))
                .content("Casual student blog post about CS classes.")
                .collectionId(KnowledgeCollectionRegistry.DEFAULT_COLLECTION_ID)
                .metadata(ArtifactMetadata.builder().category("general").domain("campus").build())
                .source(ArtifactSource.builder().title("Student Blog").sourceType("web_page").build())
                .build();

        HybridCandidate candA = new HybridCandidate(artA, 0.85, 0.90, 0.80, 1.0);
        HybridCandidate candB = new HybridCandidate(artB, 0.50, 0.50, 0.50, 0.5);

        QueryContext qc = QueryContext.builder()
                .rawQuery("CS graduation requirements")
                .intent(QueryIntent.ACADEMIC_INQUIRY)
                .confidenceScore(0.95)
                .build();

        List<ArtifactScore> ranked = rankingService.rankArtifacts(List.of(candB, candA), qc);

        assertEquals(2, ranked.size());
        assertEquals("art_A", ranked.get(0).getArtifact().getId().getValue());
        assertTrue(ranked.get(0).getTotalScore() > ranked.get(1).getTotalScore());
        assertNotNull(ranked.get(0).getExplanation());
    }
}
