package com.campusguide.personal.ai.atlas.knowledge.vector;

import com.campusguide.personal.ai.atlas.knowledge.artifact.KnowledgeArtifact;
import com.campusguide.personal.ai.atlas.knowledge.embedding.EmbeddingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Repository layer providing artifact-based search, indexing, and vector persistence operations.
 */
@Repository
public class VectorRepository {

    private final VectorStore vectorStore;
    private final EmbeddingService embeddingService;

    @Autowired
    public VectorRepository(VectorStore vectorStore, EmbeddingService embeddingService) {
        this.vectorStore = vectorStore != null ? vectorStore : new InMemoryVectorStore();
        this.embeddingService = embeddingService;
    }

    public void saveArtifact(KnowledgeArtifact artifact) {
        if (artifact == null) return;
        if (!artifact.hasEmbedding() && embeddingService != null) {
            artifact.setEmbedding(embeddingService.generateEmbedding(artifact.getContent()));
        }
        if (artifact.hasEmbedding()) {
            VectorRecord record = VectorRecord.fromArtifact(artifact);
            vectorStore.index(record);
        }
    }

    public void saveArtifacts(List<KnowledgeArtifact> artifacts) {
        if (artifacts == null) return;
        List<VectorRecord> records = new ArrayList<>();
        for (KnowledgeArtifact a : artifacts) {
            if (!a.hasEmbedding() && embeddingService != null) {
                a.setEmbedding(embeddingService.generateEmbedding(a.getContent()));
            }
            if (a.hasEmbedding()) {
                records.add(VectorRecord.fromArtifact(a));
            }
        }
        vectorStore.indexAll(records);
    }

    public List<KnowledgeArtifact> findSimilarArtifacts(String queryText, int topK, VectorMetadata filter) {
        if (queryText == null || queryText.isBlank()) return List.of();
        float[] queryVector = embeddingService != null ? embeddingService.generateEmbedding(queryText).getVector() : new float[1536];
        return findSimilarArtifacts(queryVector, topK, filter);
    }

    public List<KnowledgeArtifact> findSimilarArtifacts(float[] queryVector, int topK, VectorMetadata filter) {
        List<VectorRecord> records = vectorStore.search(queryVector, topK, filter);
        return records.stream().map(VectorRecord::getArtifact).filter(Objects::nonNull).toList();
    }

    public Optional<KnowledgeArtifact> findByArtifactId(String artifactId) {
        return vectorStore.get(artifactId).map(VectorRecord::getArtifact);
    }

    public boolean deleteByArtifactId(String artifactId) {
        return vectorStore.delete(artifactId);
    }

    public void clear() {
        vectorStore.clear();
    }

    public int count() {
        return vectorStore.count();
    }
}
