package com.campusguide.personal.ai.atlas.knowledge.graph.projection;

import com.campusguide.personal.ai.atlas.knowledge.graph.edge.RelationshipType;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeType;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Policy defining graph slicing constraints, depth limits, collection filters, and permission requirements.
 */
@Data
@Builder
public class GraphProjectionPolicy implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private int maxDepth = 3;

    @Builder.Default
    private double minEdgeStrength = 0.0;

    @Builder.Default
    private Set<String> allowedCollections = Collections.emptySet();

    @Builder.Default
    private Set<NodeType> allowedNodeTypes = Collections.emptySet();

    @Builder.Default
    private Set<RelationshipType> allowedRelationshipTypes = Collections.emptySet();

    @Builder.Default
    private Set<String> requiredPermissions = Collections.emptySet();

    @Builder.Default
    private boolean includeBidirectional = true;

    public static GraphProjectionPolicy permissive() {
        return GraphProjectionPolicy.builder()
                .maxDepth(5)
                .minEdgeStrength(0.0)
                .includeBidirectional(true)
                .build();
    }

    public static GraphProjectionPolicy strict() {
        return GraphProjectionPolicy.builder()
                .maxDepth(2)
                .minEdgeStrength(0.5)
                .includeBidirectional(false)
                .build();
    }

    public static GraphProjectionPolicy collectionBound(Set<String> collections) {
        return GraphProjectionPolicy.builder()
                .allowedCollections(collections != null ? new HashSet<>(collections) : Collections.emptySet())
                .maxDepth(3)
                .build();
    }

    public static GraphProjectionPolicy neighborhood(int depth) {
        return GraphProjectionPolicy.builder()
                .maxDepth(depth)
                .minEdgeStrength(0.1)
                .build();
    }

    public boolean isCollectionAllowed(String collectionId) {
        if (allowedCollections == null || allowedCollections.isEmpty()) {
            return true;
        }
        return collectionId != null && allowedCollections.contains(collectionId);
    }

    public boolean isNodeTypeAllowed(NodeType type) {
        if (allowedNodeTypes == null || allowedNodeTypes.isEmpty()) {
            return true;
        }
        return type != null && allowedNodeTypes.contains(type);
    }

    public boolean isRelationshipTypeAllowed(RelationshipType type) {
        if (allowedRelationshipTypes == null || allowedRelationshipTypes.isEmpty()) {
            return true;
        }
        return type != null && allowedRelationshipTypes.contains(type);
    }

    public boolean isPermissionSatisfied(Set<String> userPermissions) {
        if (requiredPermissions == null || requiredPermissions.isEmpty()) {
            return true;
        }
        if (userPermissions == null || userPermissions.isEmpty()) {
            return false;
        }
        return userPermissions.containsAll(requiredPermissions);
    }
}
