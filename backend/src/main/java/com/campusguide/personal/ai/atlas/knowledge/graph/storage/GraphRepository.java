package com.campusguide.personal.ai.atlas.knowledge.graph.storage;

import com.campusguide.personal.ai.atlas.knowledge.graph.model.KnowledgeGraph;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.KnowledgeNode;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeIdentifier;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeType;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repository layer abstraction providing querying and graph operations over the underlying GraphStore.
 */
@Repository
public class GraphRepository {

    private final GraphStore graphStore;

    public GraphRepository(GraphStore graphStore) {
        this.graphStore = graphStore;
    }

    public void save(KnowledgeGraph graph) {
        graphStore.save(graph);
    }

    public Optional<KnowledgeGraph> findById(String graphId) {
        return graphStore.findById(graphId);
    }

    public List<KnowledgeGraph> findAll() {
        return graphStore.findAll();
    }

    public boolean deleteById(String graphId) {
        return graphStore.deleteById(graphId);
    }

    public boolean existsById(String graphId) {
        return graphStore.existsById(graphId);
    }

    public List<KnowledgeGraph> findByCollectionId(String collectionId) {
        if (collectionId == null || collectionId.isBlank()) return List.of();
        return graphStore.findAll().stream()
                .filter(g -> g.getMetadata() != null && g.getMetadata().getSourceCollectionIds().contains(collectionId))
                .collect(Collectors.toList());
    }

    public Optional<KnowledgeNode> findNodeInGraph(String graphId, NodeIdentifier nodeId) {
        Optional<KnowledgeGraph> graphOpt = findById(graphId);
        return graphOpt.map(knowledgeGraph -> knowledgeGraph.getNode(nodeId));
    }

    public List<KnowledgeNode> findNodesByTypeInGraph(String graphId, NodeType type) {
        Optional<KnowledgeGraph> graphOpt = findById(graphId);
        return graphOpt.map(knowledgeGraph -> knowledgeGraph.getNodesByType(type)).orElse(List.of());
    }

    public Optional<GraphSnapshot> createSnapshot(String graphId) {
        return graphStore.createSnapshot(graphId);
    }

    public KnowledgeGraph restoreSnapshot(GraphSnapshot snapshot) {
        return graphStore.restoreSnapshot(snapshot);
    }
}
