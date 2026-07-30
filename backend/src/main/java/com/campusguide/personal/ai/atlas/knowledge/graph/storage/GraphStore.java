package com.campusguide.personal.ai.atlas.knowledge.graph.storage;

import com.campusguide.personal.ai.atlas.knowledge.graph.model.KnowledgeGraph;

import java.util.List;
import java.util.Optional;

/**
 * Storage SPI interface for persisting and retrieving provider-independent KnowledgeGraphs.
 * Designed to be compatible with property graph stores such as Neo4j, JanusGraph, Apache TinkerPop, and Amazon Neptune.
 */
public interface GraphStore {

    /**
     * Save or update a KnowledgeGraph in storage.
     */
    void save(KnowledgeGraph graph);

    /**
     * Find a KnowledgeGraph by its graphId.
     */
    Optional<KnowledgeGraph> findById(String graphId);

    /**
     * Find all persisted KnowledgeGraphs.
     */
    List<KnowledgeGraph> findAll();

    /**
     * Delete a KnowledgeGraph by its graphId.
     */
    boolean deleteById(String graphId);

    /**
     * Check if a KnowledgeGraph exists by graphId.
     */
    boolean existsById(String graphId);

    /**
     * Create a snapshot of the current state of a KnowledgeGraph.
     */
    Optional<GraphSnapshot> createSnapshot(String graphId);

    /**
     * Restore a KnowledgeGraph from a snapshot.
     */
    KnowledgeGraph restoreSnapshot(GraphSnapshot snapshot);
}
