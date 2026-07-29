package com.campusguide.personal.ai.atlas.knowledge.embedding;

import com.campusguide.personal.ai.atlas.config.AtlasProperties;
import com.campusguide.personal.ai.atlas.knowledge.artifact.ArtifactEmbedding;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmbeddingServiceTest {

    private EmbeddingService embeddingService;

    @BeforeEach
    void setUp() {
        MockEmbeddingProvider mockProvider = new MockEmbeddingProvider(1536);
        OpenAIEmbeddingProvider openAIProvider = new OpenAIEmbeddingProvider(new AtlasProperties());

        embeddingService = new EmbeddingService(List.of(mockProvider, openAIProvider), null);
    }

    @Test
    @DisplayName("Should generate deterministic vector embedding for text")
    void testSingleEmbedding() {
        ArtifactEmbedding emb = embeddingService.generateEmbedding("Campus Guide AI Infrastructure", "mock");

        assertThat(emb).isNotNull();
        assertThat(emb.getVector()).hasSize(1536);
        assertThat(emb.getProvider()).isEqualTo("mock");
        assertThat(emb.getDimension()).isEqualTo(1536);
    }

    @Test
    @DisplayName("Should cache vector embeddings for identical text queries")
    void testEmbeddingCaching() {
        String query = "Repeatable text query for caching verification";

        ArtifactEmbedding emb1 = embeddingService.generateEmbedding(query, "mock");
        int cacheSize1 = embeddingService.getCacheSize();

        ArtifactEmbedding emb2 = embeddingService.generateEmbedding(query, "mock");
        int cacheSize2 = embeddingService.getCacheSize();

        assertThat(emb1).isEqualTo(emb2);
        assertThat(cacheSize1).isEqualTo(cacheSize2);
    }

    @Test
    @DisplayName("Should process batch embedding requests correctly")
    void testBatchEmbeddings() {
        List<String> texts = List.of("First chunk", "Second chunk", "Third chunk", "Fourth chunk");
        EmbeddingRequest req = EmbeddingRequest.of(texts, "mock-model");

        List<ArtifactEmbedding> embeddings = embeddingService.generateEmbeddingsBatch(req, "mock");

        assertThat(embeddings).hasSize(4);
        for (ArtifactEmbedding e : embeddings) {
            assertThat(e.getVector()).hasSize(1536);
        }
    }
}
