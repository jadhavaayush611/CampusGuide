package com.campusguide.personal.ai.atlas.knowledge.chunking;

import com.campusguide.personal.ai.atlas.knowledge.artifact.ArtifactReference;
import com.campusguide.personal.ai.atlas.knowledge.artifact.KnowledgeArtifact;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Engine coordinating chunking operations over KnowledgeArtifacts using registered strategies.
 */
@Component
public class ChunkingEngine {

    private final Map<String, ChunkingStrategy> strategies = new HashMap<>();
    private final ChunkingStrategy defaultStrategy;

    @Autowired
    public ChunkingEngine(List<ChunkingStrategy> strategyList) {
        if (strategyList != null) {
            for (ChunkingStrategy s : strategyList) {
                strategies.put(s.getStrategyName().toUpperCase(), s);
            }
        }
        this.defaultStrategy = strategies.getOrDefault("SEMANTIC",
                strategies.getOrDefault("FIXED_SIZE", new FixedSizeChunker(null)));
    }

    public List<KnowledgeArtifact> chunk(KnowledgeArtifact documentArtifact, ChunkMetadata options) {
        if (documentArtifact == null) {
            return List.of();
        }

        ChunkMetadata opts = options != null ? options : ChunkMetadata.defaultOptions();
        String reqStrategy = opts.getStrategyName() != null ? opts.getStrategyName().toUpperCase() : "SEMANTIC";

        ChunkingStrategy strategy = strategies.getOrDefault(reqStrategy, defaultStrategy);
        List<KnowledgeArtifact> chunks = strategy.chunk(documentArtifact, opts);

        // Update document artifact with child references
        for (KnowledgeArtifact chunk : chunks) {
            documentArtifact.addReference(ArtifactReference.child(chunk.getId().getValue()));
        }

        return chunks;
    }

    public ChunkingStrategy getStrategy(String strategyName) {
        if (strategyName == null) return defaultStrategy;
        return strategies.getOrDefault(strategyName.toUpperCase(), defaultStrategy);
    }
}
