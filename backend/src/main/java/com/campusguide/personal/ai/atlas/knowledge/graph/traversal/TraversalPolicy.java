package com.campusguide.personal.ai.atlas.knowledge.graph.traversal;

import com.campusguide.personal.ai.atlas.knowledge.graph.edge.RelationshipType;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Traversal configuration controlling depth, direction, filtering, cycle detection, and limits.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraversalPolicy implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Direction {
        OUTGOING,
        INCOMING,
        BOTH
    }

    @Builder.Default
    private int maxDepth = 3;

    @Builder.Default
    private Direction direction = Direction.OUTGOING;

    @Builder.Default
    private Set<RelationshipType> allowedRelationshipTypes = new HashSet<>();

    @Builder.Default
    private Set<NodeType> allowedNodeTypes = new HashSet<>();

    @Builder.Default
    private boolean detectCycles = true;

    @Builder.Default
    private int maxNodes = 100;

    @Builder.Default
    private int maxPaths = 50;

    @Builder.Default
    private double minStrength = 0.0;

    public static TraversalPolicy defaultPolicy() {
        return TraversalPolicy.builder().build();
    }

    public static TraversalPolicy bfs(int maxDepth) {
        return TraversalPolicy.builder().maxDepth(maxDepth).build();
    }

    public static TraversalPolicy dfs(int maxDepth) {
        return TraversalPolicy.builder().maxDepth(maxDepth).build();
    }

    public boolean isRelationshipAllowed(RelationshipType type) {
        if (allowedRelationshipTypes == null || allowedRelationshipTypes.isEmpty()) {
            return true;
        }
        return allowedRelationshipTypes.contains(type);
    }

    public boolean isNodeTypeAllowed(NodeType type) {
        if (allowedNodeTypes == null || allowedNodeTypes.isEmpty()) {
            return true;
        }
        return allowedNodeTypes.contains(type);
    }
}
