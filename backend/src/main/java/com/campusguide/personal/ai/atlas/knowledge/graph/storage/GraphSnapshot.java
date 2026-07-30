package com.campusguide.personal.ai.atlas.knowledge.graph.storage;

import com.campusguide.personal.ai.atlas.knowledge.graph.edge.KnowledgeEdge;
import com.campusguide.personal.ai.atlas.knowledge.graph.model.KnowledgeGraph;
import com.campusguide.personal.ai.atlas.knowledge.graph.model.KnowledgeGraphMetadata;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.KnowledgeNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Serializable snapshot of a KnowledgeGraph at a specific point in time.
 * Designed for point-in-time recovery, graph exports, and multi-provider storage adapter persistence.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphSnapshot implements Serializable {

    private static final long serialVersionUID = 1L;

    @NonNull
    private String snapshotId;

    @NonNull
    private String graphId;

    @Builder.Default
    private String version = "1.0.0";

    private KnowledgeGraphMetadata metadata;

    @Builder.Default
    private List<KnowledgeNode> nodes = new ArrayList<>();

    @Builder.Default
    private List<KnowledgeEdge> edges = new ArrayList<>();

    @Builder.Default
    private Instant createdAt = Instant.now();

    public static GraphSnapshot fromGraph(@NonNull KnowledgeGraph graph) {
        String snapId = "snap_" + graph.getMetadata().getGraphId() + "_" + System.currentTimeMillis();
        return GraphSnapshot.builder()
                .snapshotId(snapId)
                .graphId(graph.getMetadata().getGraphId())
                .version(graph.getMetadata().getVersion())
                .metadata(graph.getMetadata())
                .nodes(graph.getNodes())
                .edges(graph.getEdges())
                .createdAt(Instant.now())
                .build();
    }

    public KnowledgeGraph toGraph() {
        KnowledgeGraph graph = new KnowledgeGraph(metadata != null ? metadata : KnowledgeGraphMetadata.builder().graphId(graphId).build());
        if (nodes != null) {
            for (KnowledgeNode node : nodes) {
                graph.addNode(node);
            }
        }
        if (edges != null) {
            for (KnowledgeEdge edge : edges) {
                graph.addEdge(edge);
            }
        }
        return graph;
    }
}
