package com.campusguide.personal.ai.atlas.knowledge.ingestion;

import com.campusguide.personal.ai.atlas.config.AtlasProperties;
import com.campusguide.personal.ai.atlas.knowledge.artifact.ArtifactLifecycleState;
import com.campusguide.personal.ai.atlas.knowledge.catalog.KnowledgeCatalog;
import com.campusguide.personal.ai.atlas.knowledge.catalog.KnowledgeCatalogEntry;
import com.campusguide.personal.ai.atlas.knowledge.catalog.KnowledgeRegistry;
import com.campusguide.personal.ai.atlas.knowledge.chunking.*;
import com.campusguide.personal.ai.atlas.knowledge.embedding.EmbeddingService;
import com.campusguide.personal.ai.atlas.knowledge.embedding.MockEmbeddingProvider;
import com.campusguide.personal.ai.atlas.knowledge.ingestion.parser.DocxDocumentParser;
import com.campusguide.personal.ai.atlas.knowledge.ingestion.parser.MarkdownDocumentParser;
import com.campusguide.personal.ai.atlas.knowledge.ingestion.parser.PdfDocumentParser;
import com.campusguide.personal.ai.atlas.knowledge.ingestion.parser.TextDocumentParser;
import com.campusguide.personal.ai.atlas.knowledge.vector.InMemoryVectorStore;
import com.campusguide.personal.ai.atlas.knowledge.vector.VectorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class KnowledgeIngestionServiceTest {

    private KnowledgeIngestionService ingestionService;
    private KnowledgeCatalog catalog;
    private VectorRepository vectorRepository;

    @BeforeEach
    void setUp() {
        DocumentLoader loader = new DocumentLoader();
        ArtifactBuilder artifactBuilder = new ArtifactBuilder();

        TextDocumentParser textParser = new TextDocumentParser();
        MarkdownDocumentParser mdParser = new MarkdownDocumentParser();
        PdfDocumentParser pdfParser = new PdfDocumentParser();
        DocxDocumentParser docxParser = new DocxDocumentParser();

        ContentExtractor contentExtractor = new ContentExtractor();
        MetadataExtractor metadataExtractor = new MetadataExtractor();

        FixedSizeChunker fixedChunker = new FixedSizeChunker(artifactBuilder);
        SlidingWindowChunker slidingChunker = new SlidingWindowChunker(artifactBuilder);
        SemanticChunker semanticChunker = new SemanticChunker(artifactBuilder);
        ChunkingEngine chunkingEngine = new ChunkingEngine(List.of(fixedChunker, slidingChunker, semanticChunker));

        MockEmbeddingProvider mockProvider = new MockEmbeddingProvider(1536);
        EmbeddingService embeddingService = new EmbeddingService(List.of(mockProvider), null);

        InMemoryVectorStore vectorStore = new InMemoryVectorStore();
        vectorRepository = new VectorRepository(vectorStore, embeddingService);

        KnowledgeRegistry registry = new KnowledgeRegistry();
        catalog = new KnowledgeCatalog(registry);

        ingestionService = new KnowledgeIngestionService(
                loader,
                List.of(textParser, mdParser, pdfParser, docxParser),
                contentExtractor,
                metadataExtractor,
                artifactBuilder,
                chunkingEngine,
                embeddingService,
                vectorRepository,
                catalog,
                null
        );
    }

    @Test
    @DisplayName("Should execute end-to-end ingestion pipeline for plain text document")
    void testEndToEndTextIngestion() {
        String text = "CAMPUS EMERGENCY PROCEDURES\n\n1. In case of fire, evacuate the building immediately via stairwells.\n\n2. Call campus security at 555-0199.";

        KnowledgeIngestionService.IngestionResult result = ingestionService.ingestText(text, "Emergency Procedures", "file://emergency.txt");

        assertThat(result.success()).isTrue();
        assertThat(result.documentId()).isNotBlank();
        assertThat(result.chunkCount()).isGreaterThan(0);

        Optional<KnowledgeCatalogEntry> entry = catalog.getEntry(result.documentId());
        assertThat(entry).isPresent();
        assertThat(entry.get().getStatus()).isEqualTo(ArtifactLifecycleState.INDEXED);
        assertThat(vectorRepository.count()).isEqualTo(result.chunkCount());
    }

    @Test
    @DisplayName("Should execute RAG search over ingested document artifacts")
    void testIngestAndSearch() {
        String md = "# Library Rules\n\nSilence must be maintained in the reading room.\n\n# Group Study\n\nReservations required for study rooms.";
        ingestionService.ingestBytes(md.getBytes(), "rules.md", "text/markdown", "file://rules.md");

        var matches = vectorRepository.findSimilarArtifacts("reading room silence rules", 1, null);

        assertThat(matches).isNotEmpty();
        assertThat(matches.get(0).getContent()).contains("Silence");
    }
}
