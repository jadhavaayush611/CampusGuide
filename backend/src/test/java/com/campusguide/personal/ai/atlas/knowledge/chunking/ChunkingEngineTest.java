package com.campusguide.personal.ai.atlas.knowledge.chunking;

import com.campusguide.personal.ai.atlas.knowledge.artifact.*;
import com.campusguide.personal.ai.atlas.knowledge.ingestion.ArtifactBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChunkingEngineTest {

    private ChunkingEngine chunkingEngine;
    private KnowledgeArtifact sampleDocument;

    @BeforeEach
    void setUp() {
        ArtifactBuilder builder = new ArtifactBuilder();
        FixedSizeChunker fixed = new FixedSizeChunker(builder);
        SlidingWindowChunker sliding = new SlidingWindowChunker(builder);
        SemanticChunker semantic = new SemanticChunker(builder);

        chunkingEngine = new ChunkingEngine(List.of(fixed, sliding, semantic));

        String content = "OVERVIEW\n\nCampusGuide is a comprehensive student platform.\n\n" +
                "ACADEMIC SERVICES\n\nAcademic services include course registration, degree planning, and tutoring.\n\n" +
                "CAMPUS FACILITIES\n\nFacilities include central library, computer labs, and sports center.";

        sampleDocument = KnowledgeArtifact.builder()
                .id(ArtifactIdentifier.of("art_doc100"))
                .type(ArtifactType.DOCUMENT)
                .content(content)
                .source(ArtifactSource.builder().title("Student Handbook").sourceUri("file://handbook.txt").build())
                .build();
    }

    @Test
    @DisplayName("FixedSizeChunker should create fixed size chunks with overlap")
    void testFixedSizeChunker() {
        ChunkMetadata opts = ChunkMetadata.builder()
                .chunkSize(100)
                .overlap(20)
                .strategyName("FIXED_SIZE")
                .build();

        List<KnowledgeArtifact> chunks = chunkingEngine.chunk(sampleDocument, opts);

        assertThat(chunks).isNotEmpty();
        assertThat(chunks.get(0).getType()).isEqualTo(ArtifactType.CHUNK);
        assertThat(chunks.get(0).getId().getValue()).startsWith("art_doc100_chk_");
        assertThat(chunks.get(0).getReferences()).isNotEmpty();
    }

    @Test
    @DisplayName("SlidingWindowChunker should create sliding word chunks")
    void testSlidingWindowChunker() {
        ChunkMetadata opts = ChunkMetadata.builder()
                .chunkSize(150)
                .overlap(30)
                .strategyName("SLIDING_WINDOW")
                .build();

        List<KnowledgeArtifact> chunks = chunkingEngine.chunk(sampleDocument, opts);

        assertThat(chunks).isNotEmpty();
        assertThat(chunks.get(0).getMetadata().get("strategy", String.class)).isEqualTo("SLIDING_WINDOW");
    }

    @Test
    @DisplayName("SemanticChunker should respect headings and paragraphs")
    void testSemanticChunker() {
        ChunkMetadata opts = ChunkMetadata.builder()
                .chunkSize(200)
                .strategyName("SEMANTIC")
                .build();

        List<KnowledgeArtifact> chunks = chunkingEngine.chunk(sampleDocument, opts);

        assertThat(chunks).isNotEmpty();
        assertThat(chunks.get(0).getMetadata().get("strategy", String.class)).isEqualTo("SEMANTIC");
        assertThat(chunks.get(0).getMetadata().containsKey("sectionHeading")).isTrue();
    }
}
