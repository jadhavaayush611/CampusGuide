package com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.context;

import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeIdentifier;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Encapsulates the target goal, target nodes, and objective type of a graph reasoning request.
 */
@Data
@Builder
public class ReasoningObjective implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum ObjectiveType {
        EXPLAIN_RELATIONSHIP,
        FIND_PATH,
        DISCOVER_INDIRECT_CONNECTIONS,
        VERIFY_INVARIANT,
        INFER_MISSING_LINKS,
        CONTEXT_GENERATION,
        CUSTOM
    }

    private final ObjectiveType type;
    private final String description;
    private final Set<NodeIdentifier> targetNodes;

    @Builder.Default
    private final Map<String, Object> parameters = new HashMap<>();

    public static ReasoningObjective explainRelationship(Set<NodeIdentifier> targets) {
        return ReasoningObjective.builder()
                .type(ObjectiveType.EXPLAIN_RELATIONSHIP)
                .description("Explain explicit or implicit relationships between nodes")
                .targetNodes(targets != null ? targets : Collections.emptySet())
                .build();
    }

    public static ReasoningObjective findPath(NodeIdentifier source, NodeIdentifier target) {
        return ReasoningObjective.builder()
                .type(ObjectiveType.FIND_PATH)
                .description("Find optimal reasoning path between source and target node")
                .targetNodes(Set.of(source, target))
                .build();
    }

    public static ReasoningObjective discoverIndirectConnections(Set<NodeIdentifier> roots) {
        return ReasoningObjective.builder()
                .type(ObjectiveType.DISCOVER_INDIRECT_CONNECTIONS)
                .description("Discover multi-hop indirect relationships across domains")
                .targetNodes(roots != null ? roots : Collections.emptySet())
                .build();
    }

    public static ReasoningObjective contextGeneration() {
        return ReasoningObjective.builder()
                .type(ObjectiveType.CONTEXT_GENERATION)
                .description("Generate graph-driven context evidence for LLM prompt context")
                .targetNodes(Collections.emptySet())
                .build();
    }
}
