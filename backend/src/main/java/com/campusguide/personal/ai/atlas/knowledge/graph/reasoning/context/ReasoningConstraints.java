package com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.context;

import com.campusguide.personal.ai.atlas.knowledge.graph.edge.RelationshipType;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

/**
 * Execution bounds and budget constraints for graph reasoning operations.
 */
@Data
@Builder
public class ReasoningConstraints implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private int maxDepth = 4;

    @Builder.Default
    private double confidenceThreshold = 0.5;

    @Builder.Default
    private long maxExecutionTimeMs = 1000L;

    @Builder.Default
    private int maxPathsToExplore = 20;

    @Builder.Default
    private Set<String> requiredCollections = Collections.emptySet();

    @Builder.Default
    private Set<RelationshipType> forbiddenRelationshipTypes = Collections.emptySet();

    public static ReasoningConstraints defaultConstraints() {
        return ReasoningConstraints.builder().build();
    }
}
