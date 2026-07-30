package com.campusguide.personal.ai.atlas.knowledge.graph.model;

import com.campusguide.personal.ai.atlas.knowledge.graph.lifecycle.GraphDiagnostics;
import com.campusguide.personal.ai.atlas.knowledge.graph.lifecycle.GraphLifecycleState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * High-level metadata for a KnowledgeGraph instance.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeGraphMetadata implements Serializable {

    private static final long serialVersionUID = 1L;

    @NonNull
    private String graphId;

    @Builder.Default
    private String name = "Default Knowledge Graph";

    @Builder.Default
    private String version = "1.0.0";

    @Builder.Default
    private GraphLifecycleState lifecycleState = GraphLifecycleState.DISCOVERED;

    @Builder.Default
    private Set<String> sourceCollectionIds = new HashSet<>();

    @Builder.Default
    private int nodeCount = 0;

    @Builder.Default
    private int edgeCount = 0;

    @Builder.Default
    private GraphDiagnostics diagnostics = new GraphDiagnostics();

    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private Instant updatedAt = Instant.now();

    public void addSourceCollection(String collectionId) {
        if (this.sourceCollectionIds == null) {
            this.sourceCollectionIds = new HashSet<>();
        }
        if (collectionId != null && !collectionId.isBlank()) {
            this.sourceCollectionIds.add(collectionId);
        }
    }
}
