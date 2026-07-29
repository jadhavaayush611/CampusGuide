package com.campusguide.personal.ai.atlas.knowledge.retrieval.semantic;

import com.campusguide.personal.ai.atlas.knowledge.artifact.ArtifactEmbedding;
import com.campusguide.personal.ai.atlas.knowledge.artifact.ArtifactIdentifier;
import com.campusguide.personal.ai.atlas.knowledge.artifact.KnowledgeArtifact;
import com.campusguide.personal.ai.atlas.knowledge.embedding.MockEmbeddingProvider;
import com.campusguide.personal.ai.atlas.knowledge.retrieval.semantic.SemanticRetriever.SemanticMatch;
import com.campusguide.personal.ai.atlas.knowledge.vector.InMemoryVectorStore;
import com.campusguide.personal.ai.atlas.knowledge.vector.VectorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SemanticRetrieverTest {

    private VectorRepository vectorRepository;
    private MockEmbeddingProvider mockEmbeddingProvider;
    private VectorRetriever vectorRetriever;
    private SemanticRetriever semanticRetriever;

    @BeforeEach
    void setUp() {
        InMemoryVectorStore store = new InMemoryVectorStore();
        mockEmbeddingProvider = new MockEmbeddingProvider();
        com.campusguide.personal.ai.atlas.knowledge.embedding.EmbeddingService embeddingService =
                new com.campusguide.personal.ai.atlas.knowledge.embedding.EmbeddingService(List.of(mockEmbeddingProvider), null);
        vectorRepository = new VectorRepository(store, embeddingService);
        vectorRetriever = new VectorRetriever(store);
        semanticRetriever = new SemanticRetriever(vectorRetriever, embeddingService);
    }

    @Test
    @DisplayName("Should perform vector similarity retrieval and threshold filtering")
    void testSemanticRetrievalWithSimilarityThreshold() {
        String content = "The Campus Library is open Monday to Friday 8am - 10pm.";
        ArtifactEmbedding emb = mockEmbeddingProvider.embed(com.campusguide.personal.ai.atlas.knowledge.embedding.EmbeddingRequest.of(content)).getEmbeddings().get(0);

        KnowledgeArtifact artifact = KnowledgeArtifact.builder()
                .id(ArtifactIdentifier.of("art_lib_01"))
                .content(content)
                .collectionId("public_campus_knowledge")
                .embedding(emb)
                .build();

        vectorRepository.saveArtifact(artifact);

        List<SemanticMatch> matches = semanticRetriever.retrieveSemantic(content, 5, 0.50, null);

        assertFalse(matches.isEmpty());
        assertEquals("art_lib_01", matches.get(0).artifact().getId().getValue());
        assertTrue(matches.get(0).similarityScore() >= 0.50);
    }
}
