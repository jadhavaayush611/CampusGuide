package com.campusguide.personal.ai.atlas.knowledge.graph.registry;

import com.campusguide.personal.ai.atlas.knowledge.graph.lifecycle.GraphDiagnostics;
import com.campusguide.personal.ai.atlas.knowledge.graph.lifecycle.GraphLifecycleState;
import com.campusguide.personal.ai.atlas.knowledge.graph.model.KnowledgeGraph;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Catalog entry representing metadata, statistics, and lifecycle state of a KnowledgeGraph.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphCatalogEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    private String graphId;
    private String name;
    private String version;
    private GraphLifecycleState lifecycleState;
    private int nodeCount;
    private int edgeCount;

    @Builder.Default
    private Set<String> sourceCollectionIds = new HashSet<>();

    private GraphDiagnostics lastDiagnostics;

    private Instant createdAt;
    private Instant updatedAt;

    public static GraphCatalogEntry fromGraph(KnowledgeGraph graph) {
        if (graph == null || graph.getMetadata() == null) {
            return null;
        }
        return GraphCatalogEntry.builder()
                .graphId(graph.getMetadata().getGraphId())
                .name(graph.getMetadata().getName())
                .version(graph.getMetadata().getVersion())
                .lifecycleState(graph.getMetadata().getLifecycleState())
                .nodeCount(graph.getMetadata().getNodeCount())
                .edgeCount(graph.getMetadata().getEdgeCount())
                .sourceCollectionIds(new HashSet<>(graph.getMetadata().getSourceCollectionIds()))
                .lastDiagnostics(graph.getMetadata().getDiagnostics())
                .createdAt(graph.getMetadata().getCreatedAt())
                .updatedAt(graph.getMetadata().getUpdatedAt())
                .build();
    }
}
