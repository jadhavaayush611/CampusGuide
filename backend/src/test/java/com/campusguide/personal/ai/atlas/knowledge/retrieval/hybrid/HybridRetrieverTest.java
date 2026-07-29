package com.campusguide.personal.ai.atlas.knowledge.retrieval.hybrid;

import com.campusguide.personal.ai.atlas.knowledge.artifact.ArtifactEmbedding;
import com.campusguide.personal.ai.atlas.knowledge.artifact.ArtifactIdentifier;
import com.campusguide.personal.ai.atlas.knowledge.artifact.ArtifactMetadata;
import com.campusguide.personal.ai.atlas.knowledge.artifact.ArtifactSource;
import com.campusguide.personal.ai.atlas.knowledge.artifact.KnowledgeArtifact;
import com.campusguide.personal.ai.atlas.knowledge.collection.KnowledgeCollection;
import com.campusguide.personal.ai.atlas.knowledge.collection.KnowledgeCollectionRegistry;
import com.campusguide.personal.ai.atlas.knowledge.embedding.EmbeddingService;
import com.campusguide.personal.ai.atlas.knowledge.embedding.MockEmbeddingProvider;
import com.campusguide.personal.ai.atlas.knowledge.retrieval.collection.CollectionSelector.KnowledgeCollectionSelection;
import com.campusguide.personal.ai.atlas.knowledge.retrieval.hybrid.HybridRankingEngine.HybridCandidate;
import com.campusguide.personal.ai.atlas.knowledge.retrieval.semantic.SemanticRetriever;
import com.campusguide.personal.ai.atlas.knowledge.retrieval.semantic.VectorRetriever;
import com.campusguide.personal.ai.atlas.knowledge.vector.InMemoryVectorStore;
import com.campusguide.personal.ai.atlas.knowledge.vector.VectorMetadata;
import com.campusguide.personal.ai.atlas.knowledge.vector.VectorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HybridRetrieverTest {

    private VectorRepository vectorRepository;
    private MockEmbeddingProvider mockEmbeddingProvider;
    private HybridRetriever hybridRetriever;
    private KnowledgeCollectionRegistry registry;

    @BeforeEach
    void setUp() {
        InMemoryVectorStore store = new InMemoryVectorStore();
        mockEmbeddingProvider = new MockEmbeddingProvider();
        EmbeddingService embeddingService = new EmbeddingService(List.of(mockEmbeddingProvider), null);
        vectorRepository = new VectorRepository(store, embeddingService);
        VectorRetriever vectorRetriever = new VectorRetriever(store);
        SemanticRetriever semanticRetriever = new SemanticRetriever(vectorRetriever, embeddingService);
        HybridRankingEngine rankingEngine = new HybridRankingEngine();
        hybridRetriever = new HybridRetriever(semanticRetriever, vectorRepository, rankingEngine);
        registry = new KnowledgeCollectionRegistry();
    }

    @Test
    @DisplayName("Should retrieve unified ranked list combining vector similarity and keyword overlap")
    void testHybridRetrievalCombinedScoring() {
        String query = "science hall office hours";
        ArtifactEmbedding emb = mockEmbeddingProvider.embed(com.campusguide.personal.ai.atlas.knowledge.embedding.EmbeddingRequest.of(query)).getEmbeddings().get(0);

        KnowledgeArtifact art1 = KnowledgeArtifact.builder()
                .id(ArtifactIdentifier.of("art_science_01"))
                .content("Science Hall faculty office hours are posted outside room 204.")
                .collectionId(KnowledgeCollectionRegistry.PUBLIC_CAMPUS_KNOWLEDGE)
                .metadata(ArtifactMetadata.builder().category("campus_info").domain("campus").build())
                .source(ArtifactSource.builder().title("Science Hall Office Hours Guide").sourceType("text").build())
                .embedding(emb)
                .build();

        vectorRepository.saveArtifact(art1);

        KnowledgeCollection col = registry.getCollection(KnowledgeCollectionRegistry.PUBLIC_CAMPUS_KNOWLEDGE).orElseThrow();
        List<KnowledgeCollectionSelection> selections = List.of(new KnowledgeCollectionSelection(col, 1.2));

        VectorMetadata filter = VectorMetadata.builder().category("campus_info").build();

        List<HybridCandidate> results = hybridRetriever.retrieveHybrid(query, selections, 5, 0.0, filter);

        assertFalse(results.isEmpty());
        assertEquals("art_science_01", results.get(0).artifact().getId().getValue());
        assertTrue(results.get(0).hybridScore() > 0.0);
    }
}
