package com.campusguide.personal.ai.atlas.knowledge.graph.construction;

import com.campusguide.personal.ai.atlas.knowledge.graph.edge.KnowledgeEdge;
import com.campusguide.personal.ai.atlas.knowledge.graph.lifecycle.GraphLifecycleState;
import com.campusguide.personal.ai.atlas.knowledge.graph.model.KnowledgeGraph;
import com.campusguide.personal.ai.atlas.knowledge.graph.model.KnowledgeGraphMetadata;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.KnowledgeNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Fluent builder for step-by-step KnowledgeGraph construction.
 */
public class KnowledgeGraphBuilder {

    private String graphId;
    private String name;
    private String version = "1.0.0";
    private final List<KnowledgeNode> nodes = new ArrayList<>();
    private final List<KnowledgeEdge> edges = new ArrayList<>();
    private final List<String> sourceCollectionIds = new ArrayList<>();

    public static KnowledgeGraphBuilder builder() {
        return new KnowledgeGraphBuilder();
    }

    public KnowledgeGraphBuilder graphId(String graphId) {
        this.graphId = graphId;
        return this;
    }

    public KnowledgeGraphBuilder name(String name) {
        this.name = name;
        return this;
    }

    public KnowledgeGraphBuilder version(String version) {
        this.version = version;
        return this;
    }

    public KnowledgeGraphBuilder addNode(KnowledgeNode node) {
        if (node != null) {
            this.nodes.add(node);
        }
        return this;
    }

    public KnowledgeGraphBuilder addNodes(Collection<KnowledgeNode> nodes) {
        if (nodes != null) {
            this.nodes.addAll(nodes);
        }
        return this;
    }

    public KnowledgeGraphBuilder addEdge(KnowledgeEdge edge) {
        if (edge != null) {
            this.edges.add(edge);
        }
        return this;
    }

    public KnowledgeGraphBuilder addEdges(Collection<KnowledgeEdge> edges) {
        if (edges != null) {
            this.edges.addAll(edges);
        }
        return this;
    }

    public KnowledgeGraphBuilder addSourceCollection(String collectionId) {
        if (collectionId != null && !collectionId.isBlank()) {
            this.sourceCollectionIds.add(collectionId);
        }
        return this;
    }

    public KnowledgeGraph build() {
        if (graphId == null || graphId.isBlank()) {
            graphId = "graph_" + java.util.UUID.randomUUID().toString().substring(0, 8);
        }
        if (name == null || name.isBlank()) {
            name = graphId;
        }

        KnowledgeGraphMetadata meta = KnowledgeGraphMetadata.builder()
                .graphId(graphId)
                .name(name)
                .version(version)
                .lifecycleState(GraphLifecycleState.BUILDING)
                .build();

        for (String colId : sourceCollectionIds) {
            meta.addSourceCollection(colId);
        }

        KnowledgeGraph graph = new KnowledgeGraph(meta);

        // Add & merge nodes
        for (KnowledgeNode node : nodes) {
            graph.mergeNode(node);
        }

        // Add & deduplicate edges
        for (KnowledgeEdge edge : edges) {
            graph.deduplicateEdge(edge);
        }

        // Validate consistency
        int removedDangling = graph.validateConsistency();
        graph.getMetadata().getDiagnostics().setDanglingEdgesRemoved(removedDangling);
        graph.getMetadata().setLifecycleState(GraphLifecycleState.ACTIVE);

        return graph;
    }
}
