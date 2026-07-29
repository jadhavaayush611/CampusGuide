package com.campusguide.personal.ai.atlas.knowledge.retrieval.semantic;

import com.campusguide.personal.ai.atlas.knowledge.artifact.ArtifactEmbedding;
import com.campusguide.personal.ai.atlas.knowledge.artifact.KnowledgeArtifact;
import com.campusguide.personal.ai.atlas.knowledge.embedding.EmbeddingService;
import com.campusguide.personal.ai.atlas.knowledge.vector.VectorMetadata;
import com.campusguide.personal.ai.atlas.knowledge.vector.VectorRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Core semantic retriever performing vector similarity search, cosine score thresholding,
 * metadata filtering, and top-K KnowledgeArtifact retrieval.
 */
@Component
@Slf4j
public class SemanticRetriever {

    private final VectorRetriever vectorRetriever;
    private final EmbeddingService embeddingService;

    @Autowired
    public SemanticRetriever(VectorRetriever vectorRetriever, EmbeddingService embeddingService) {
        this.vectorRetriever = vectorRetriever;
        this.embeddingService = embeddingService;
    }

    public List<SemanticMatch> retrieveSemantic(String queryText, int topK, double minSimilarity, VectorMetadata filter) {
        if (queryText == null || queryText.isBlank() || topK <= 0) {
            return List.of();
        }

        float[] queryVector = null;
        if (embeddingService != null) {
            ArtifactEmbedding emb = embeddingService.generateEmbedding(queryText);
            if (emb != null) {
                queryVector = emb.getVector();
            }
        }

        if (queryVector == null || queryVector.length == 0) {
            log.warn("Failed to generate query embedding for semantic retrieval");
            return List.of();
        }

        return retrieveSemantic(queryVector, topK, minSimilarity, filter);
    }

    public List<SemanticMatch> retrieveSemantic(float[] queryVector, int topK, double minSimilarity, VectorMetadata filter) {
        if (queryVector == null || queryVector.length == 0 || topK <= 0) {
            return List.of();
        }

        List<VectorRecord> records = vectorRetriever.searchVectors(queryVector, topK, filter);

        List<SemanticMatch> matches = new ArrayList<>();
        for (VectorRecord r : records) {
            double sim = r.getMetadata() != null && r.getMetadata().getScore() != null ? r.getMetadata().getScore() : 0.0;
            if (sim >= minSimilarity && r.getArtifact() != null) {
                matches.add(new SemanticMatch(r.getArtifact(), sim));
            }
        }

        return matches;
    }

    public record SemanticMatch(KnowledgeArtifact artifact, double similarityScore) {}
}
