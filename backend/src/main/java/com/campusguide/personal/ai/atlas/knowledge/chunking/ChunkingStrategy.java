package com.campusguide.personal.ai.atlas.knowledge.chunking;

import com.campusguide.personal.ai.atlas.knowledge.artifact.KnowledgeArtifact;

import java.util.List;

/**
 * Interface for splitting KnowledgeArtifacts into chunk KnowledgeArtifacts.
 */
public interface ChunkingStrategy {

    /**
     * Splitting logic transforming parent document artifact into child chunk artifacts.
     */
    List<KnowledgeArtifact> chunk(KnowledgeArtifact documentArtifact, ChunkMetadata options);

    /**
     * Unique identifier/name of this chunking strategy.
     */
    String getStrategyName();
}
