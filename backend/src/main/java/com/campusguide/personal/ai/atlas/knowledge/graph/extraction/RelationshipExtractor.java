package com.campusguide.personal.ai.atlas.knowledge.graph.extraction;

import com.campusguide.personal.ai.atlas.knowledge.artifact.KnowledgeArtifact;
import com.campusguide.personal.ai.atlas.knowledge.collection.KnowledgeCollection;
import com.campusguide.personal.ai.atlas.knowledge.graph.edge.KnowledgeEdge;

import java.util.List;

/**
 * Interface for extracting structural and explicit KnowledgeEdges from entities, artifacts, and collections.
 */
public interface RelationshipExtractor {

    /**
     * Name of the extractor for provenance tracking.
     */
    String getName();

    /**
     * Extract relationships from a single KnowledgeArtifact.
     */
    List<KnowledgeEdge> extractFromArtifact(KnowledgeArtifact artifact);

    /**
     * Extract relationships between a collection and its contained artifacts.
     */
    List<KnowledgeEdge> extractFromCollection(KnowledgeCollection collection, List<KnowledgeArtifact> artifacts);

    /**
     * Extract relationships from arbitrary structured objects.
     */
    List<KnowledgeEdge> extractFromEntity(Object entity);

    /**
     * Priority order for execution. Lower value = higher priority.
     */
    default int getPriority() {
        return 100;
    }
}
