package com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.context;

import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeIdentifier;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeType;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

/**
 * Defines domain boundaries, allowed node types, and target scope for reasoning operations.
 */
@Data
@Builder
public class ReasoningScope implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String domain = "general";

    @Builder.Default
    private Set<NodeIdentifier> scopeBoundaryNodeIds = Collections.emptySet();

    @Builder.Default
    private Set<String> targetCollections = Collections.emptySet();

    @Builder.Default
    private Set<NodeType> targetNodeTypes = Collections.emptySet();

    @Builder.Default
    private boolean allowCrossDomainTraversal = true;

    public static ReasoningScope defaultScope() {
        return ReasoningScope.builder().build();
    }
}
