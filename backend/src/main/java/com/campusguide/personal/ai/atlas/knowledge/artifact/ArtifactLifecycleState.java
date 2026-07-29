package com.campusguide.personal.ai.atlas.knowledge.artifact;

/**
 * Tracks the lifecycle transitions of a KnowledgeArtifact through the ingestion pipeline.
 */
public enum ArtifactLifecycleState {
    DISCOVERED,
    INGESTING,
    PARSED,
    CHUNKED,
    EMBEDDED,
    INDEXED,
    FAILED,
    ARCHIVED;

    public boolean isTerminal() {
        return this == INDEXED || this == FAILED || this == ARCHIVED;
    }
}
