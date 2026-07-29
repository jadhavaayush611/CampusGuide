package com.campusguide.personal.ai.atlas.knowledge.vector;

import com.campusguide.personal.ai.atlas.knowledge.artifact.*;
import com.campusguide.personal.ai.atlas.knowledge.embedding.MockEmbeddingProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VectorStorageTest {

    private InMemoryVectorStore vectorStore;
    private MockEmbeddingProvider mockEmbeddingProvider;

    @BeforeEach
    void setUp() {
        vectorStore = new InMemoryVectorStore();
        mockEmbeddingProvider = new MockEmbeddingProvider(1536);
    }

    @Test
    @DisplayName("Should index vector records and retrieve top-K search results by cosine similarity")
    void testVectorSearch() {
        ArtifactEmbedding emb1 = mockEmbeddingProvider.embed(com.campusguide.personal.ai.atlas.knowledge.embedding.EmbeddingRequest.of("Computer Science curriculum")).getEmbeddings().get(0);
        ArtifactEmbedding emb2 = mockEmbeddingProvider.embed(com.campusguide.personal.ai.atlas.knowledge.embedding.EmbeddingRequest.of("Student housing and dormitories")).getEmbeddings().get(0);

        KnowledgeArtifact art1 = KnowledgeArtifact.builder()
                .id(ArtifactIdentifier.of("art_cs101"))
                .content("Computer Science curriculum")
                .embedding(emb1)
                .source(ArtifactSource.builder().sourceType("PDF").build())
                .metadata(ArtifactMetadata.builder().domain("academic").category("syllabus").build())
                .build();

        KnowledgeArtifact art2 = KnowledgeArtifact.builder()
                .id(ArtifactIdentifier.of("art_housing"))
                .content("Student housing and dormitories")
                .embedding(emb2)
                .source(ArtifactSource.builder().sourceType("DOCX").build())
                .metadata(ArtifactMetadata.builder().domain("campus").category("housing").build())
                .build();

        vectorStore.index(VectorRecord.fromArtifact(art1));
        vectorStore.index(VectorRecord.fromArtifact(art2));

        assertThat(vectorStore.count()).isEqualTo(2);

        float[] queryVec = emb1.getVector();
        List<VectorRecord> results = vectorStore.search(queryVec, 1, null);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getArtifactId()).isEqualTo("art_cs101");
        assertThat(results.get(0).getMetadata().getScore()).isGreaterThan(0.9);
    }

    @Test
    @DisplayName("Should filter vector search results by metadata fields")
    void testMetadataFiltering() {
        ArtifactEmbedding emb = mockEmbeddingProvider.embed(com.campusguide.personal.ai.atlas.knowledge.embedding.EmbeddingRequest.of("General Info")).getEmbeddings().get(0);

        KnowledgeArtifact art1 = KnowledgeArtifact.builder()
                .id(ArtifactIdentifier.of("art_1"))
                .content("General Info 1")
                .embedding(emb)
                .metadata(ArtifactMetadata.builder().domain("academic").category("guide").build())
                .source(ArtifactSource.builder().sourceType("PDF").build())
                .build();

        KnowledgeArtifact art2 = KnowledgeArtifact.builder()
                .id(ArtifactIdentifier.of("art_2"))
                .content("General Info 2")
                .embedding(emb)
                .metadata(ArtifactMetadata.builder().domain("events").category("guide").build())
                .source(ArtifactSource.builder().sourceType("PDF").build())
                .build();

        vectorStore.index(VectorRecord.fromArtifact(art1));
        vectorStore.index(VectorRecord.fromArtifact(art2));

        VectorMetadata filter = VectorMetadata.builder().domain("academic").build();
        List<VectorRecord> results = vectorStore.search(emb.getVector(), 10, filter);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getArtifactId()).isEqualTo("art_1");
    }
}
