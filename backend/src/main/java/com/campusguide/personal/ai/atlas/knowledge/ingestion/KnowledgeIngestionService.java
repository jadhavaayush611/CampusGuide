package com.campusguide.personal.ai.atlas.knowledge.ingestion;

import com.campusguide.personal.ai.atlas.knowledge.artifact.*;
import com.campusguide.personal.ai.atlas.knowledge.catalog.KnowledgeCatalog;
import com.campusguide.personal.ai.atlas.knowledge.catalog.KnowledgeCatalogEntry;
import com.campusguide.personal.ai.atlas.knowledge.chunking.ChunkMetadata;
import com.campusguide.personal.ai.atlas.knowledge.chunking.ChunkingEngine;
import com.campusguide.personal.ai.atlas.knowledge.embedding.EmbeddingService;
import com.campusguide.personal.ai.atlas.knowledge.ingestion.parser.DocumentParser;
import com.campusguide.personal.ai.atlas.knowledge.vector.VectorRepository;
import com.campusguide.personal.ai.atlas.metrics.AtlasMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Orchestrator service running the end-to-end RAG ingestion pipeline:
 * DocumentLoading -> Parsing -> Artifact Building -> Chunking -> Embedding -> Vector Indexing -> Cataloging.
 */
@Service
@Slf4j
public class KnowledgeIngestionService {

    private final DocumentLoader documentLoader;
    private final List<DocumentParser> parsers;
    private final ContentExtractor contentExtractor;
    private final MetadataExtractor metadataExtractor;
    private final ArtifactBuilder artifactBuilder;
    private final ChunkingEngine chunkingEngine;
    private final EmbeddingService embeddingService;
    private final VectorRepository vectorRepository;
    private final KnowledgeCatalog catalog;
    private final AtlasMetrics metrics;

    @Autowired
    public KnowledgeIngestionService(
            DocumentLoader documentLoader,
            List<DocumentParser> parsers,
            ContentExtractor contentExtractor,
            MetadataExtractor metadataExtractor,
            ArtifactBuilder artifactBuilder,
            ChunkingEngine chunkingEngine,
            EmbeddingService embeddingService,
            VectorRepository vectorRepository,
            KnowledgeCatalog catalog,
            AtlasMetrics metrics) {
        this.documentLoader = documentLoader;
        this.parsers = parsers != null ? parsers : List.of();
        this.contentExtractor = contentExtractor;
        this.metadataExtractor = metadataExtractor;
        this.artifactBuilder = artifactBuilder;
        this.chunkingEngine = chunkingEngine;
        this.embeddingService = embeddingService;
        this.vectorRepository = vectorRepository;
        this.catalog = catalog;
        this.metrics = metrics;
    }

    public IngestionResult ingestText(String text, String title, String sourceUri) {
        return ingestText(text, title, sourceUri, "default_collection");
    }

    public IngestionResult ingestText(String text, String title, String sourceUri, String collectionId) {
        RawDocument raw = documentLoader.loadFromText(text, title, sourceUri);
        if (collectionId != null) {
            raw.getAttributes().put("collectionId", collectionId);
        }
        return ingest(raw, ChunkMetadata.defaultOptions(), "mock");
    }

    public IngestionResult ingestBytes(byte[] bytes, String filename, String mimeType, String sourceUri) {
        RawDocument raw = documentLoader.loadFromBytes(bytes, filename, mimeType, sourceUri);
        return ingest(raw, ChunkMetadata.defaultOptions(), "mock");
    }

    public IngestionResult ingest(RawDocument rawDocument, ChunkMetadata chunkOptions, String embeddingProviderName) {
        long startTime = System.currentTimeMillis();
        String docChecksum = (String) rawDocument.getAttributes().getOrDefault("checksum",
                ArtifactVersion.computeChecksum(rawDocument.getTextContent()));

        log.info("Starting ingestion for document URI: {}, checksum: {}", rawDocument.getUri(), docChecksum);

        // Check if document already exists unchanged in catalog
        Optional<KnowledgeCatalogEntry> existing = catalog.findByChecksum(docChecksum);
        if (existing.isPresent() && existing.get().getStatus() == ArtifactLifecycleState.INDEXED) {
            log.info("Document with checksum {} already fully indexed. Skipping re-ingestion.", docChecksum);
            return IngestionResult.success(existing.get().getDocumentId(), existing.get().getIndexedArtifactIds().size(), System.currentTimeMillis() - startTime);
        }

        KnowledgeCatalogEntry catalogEntry = KnowledgeCatalogEntry.builder()
                .documentId("doc_" + System.currentTimeMillis() + "_" + Math.abs(docChecksum.hashCode()))
                .sourceUri(rawDocument.getUri())
                .sourceType(rawDocument.getMimeType())
                .title(rawDocument.getTitle())
                .checksum(docChecksum)
                .status(ArtifactLifecycleState.DISCOVERED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        catalog.registerEntry(catalogEntry);

        try {
            catalogEntry.transitionState(ArtifactLifecycleState.INGESTING, "Ingestion initiated");

            // 1. Select matching parser
            DocumentParser parser = parsers.stream()
                    .filter(p -> p.supports(rawDocument))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No suitable DocumentParser found for document: " + rawDocument.getFilename()));

            // 2. Parse Document
            long parseStart = System.currentTimeMillis();
            ParsedDocument parsedDoc = parser.parse(rawDocument);
            long parseDuration = System.currentTimeMillis() - parseStart;
            catalogEntry.transitionState(ArtifactLifecycleState.PARSED, "Parsed in " + parseDuration + " ms");

            // 3. Build Root KnowledgeArtifact
            ArtifactMetadata metadata = metadataExtractor.extractMetadata(rawDocument, parsedDoc);
            ArtifactSource source = metadataExtractor.extractSource(rawDocument, parsedDoc);
            KnowledgeArtifact rootArtifact = artifactBuilder.buildDocumentArtifact(rawDocument, parsedDoc, metadata, source);
            rootArtifact.setId(ArtifactIdentifier.of(catalogEntry.getDocumentId()));
            String collectionId = (String) rawDocument.getAttributes().getOrDefault("collectionId", "default_collection");
            rootArtifact.setCollectionId(collectionId);

            // 4. Chunk Artifact
            long chunkStart = System.currentTimeMillis();
            List<KnowledgeArtifact> chunkArtifacts = chunkingEngine.chunk(rootArtifact, chunkOptions);
            long chunkDuration = System.currentTimeMillis() - chunkStart;
            catalogEntry.setTotalChunks(chunkArtifacts.size());
            catalogEntry.transitionState(ArtifactLifecycleState.CHUNKED, "Generated " + chunkArtifacts.size() + " chunks in " + chunkDuration + " ms");

            // 5. Generate Vector Embeddings
            long embedStart = System.currentTimeMillis();
            List<String> chunkTexts = chunkArtifacts.stream().map(KnowledgeArtifact::getContent).toList();
            com.campusguide.personal.ai.atlas.knowledge.embedding.EmbeddingRequest embedReq =
                    com.campusguide.personal.ai.atlas.knowledge.embedding.EmbeddingRequest.of(chunkTexts, "text-embedding-3-small");
            List<ArtifactEmbedding> embeddings = embeddingService.generateEmbeddingsBatch(embedReq, embeddingProviderName);

            for (int i = 0; i < chunkArtifacts.size(); i++) {
                chunkArtifacts.get(i).setCollectionId(collectionId);
                if (i < embeddings.size()) {
                    chunkArtifacts.get(i).setEmbedding(embeddings.get(i));
                    chunkArtifacts.get(i).updateLifecycle(ArtifactLifecycleState.EMBEDDED);
                }
            }
            long embedDuration = System.currentTimeMillis() - embedStart;
            catalogEntry.transitionState(ArtifactLifecycleState.EMBEDDED, "Embedded in " + embedDuration + " ms");

            // 6. Index into Vector Store
            long indexStart = System.currentTimeMillis();
            vectorRepository.saveArtifacts(chunkArtifacts);
            long indexDuration = System.currentTimeMillis() - indexStart;

            List<String> indexedIds = chunkArtifacts.stream().map(a -> a.getId().getValue()).toList();
            catalogEntry.setIndexedArtifactIds(indexedIds);
            catalogEntry.transitionState(ArtifactLifecycleState.INDEXED, "Indexed " + indexedIds.size() + " vector records in " + indexDuration + " ms");

            long totalDuration = System.currentTimeMillis() - startTime;
            if (metrics != null) {
                metrics.recordOrchestrationLatency(totalDuration);
            }

            log.info("Ingestion completed successfully for docId: {}, total chunks: {}, duration: {} ms",
                    catalogEntry.getDocumentId(), chunkArtifacts.size(), totalDuration);

            return IngestionResult.success(catalogEntry.getDocumentId(), chunkArtifacts.size(), totalDuration);

        } catch (Exception e) {
            log.error("Ingestion failed for document URI {}: {}", rawDocument.getUri(), e.getMessage());
            catalogEntry.transitionState(ArtifactLifecycleState.FAILED, e.getMessage());
            return IngestionResult.failure(catalogEntry.getDocumentId(), e.getMessage(), System.currentTimeMillis() - startTime);
        }
    }

    public record IngestionResult(
            boolean success,
            String documentId,
            int chunkCount,
            long durationMs,
            String errorMessage
    ) {
        public static IngestionResult success(String documentId, int chunkCount, long durationMs) {
            return new IngestionResult(true, documentId, chunkCount, durationMs, null);
        }

        public static IngestionResult failure(String documentId, String errorMessage, long durationMs) {
            return new IngestionResult(false, documentId, 0, durationMs, errorMessage);
        }
    }
}
