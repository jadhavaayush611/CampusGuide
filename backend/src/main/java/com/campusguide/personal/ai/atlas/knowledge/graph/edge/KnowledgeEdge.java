package com.campusguide.personal.ai.atlas.knowledge.graph.edge;

import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeIdentifier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.Serializable;
import java.time.Instant;

/**
 * Core edge entity in the Atlas Knowledge Graph representing a relationship between two KnowledgeNodes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeEdge implements Serializable {

    private static final long serialVersionUID = 1L;

    @NonNull
    private String id;

    @NonNull
    private NodeIdentifier sourceNodeId;

    @NonNull
    private NodeIdentifier targetNodeId;

    @NonNull
    @Builder.Default
    private RelationshipType relationshipType = RelationshipType.RELATED_TO;

    @NonNull
    @Builder.Default
    private RelationshipStrength strength = RelationshipStrength.MEDIUM;

    @Builder.Default
    private boolean bidirectional = false;

    @Builder.Default
    private EdgeMetadata metadata = new EdgeMetadata();

    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private Instant updatedAt = Instant.now();

    public static String generateId(@NonNull NodeIdentifier source, @NonNull NodeIdentifier target, @NonNull RelationshipType type) {
        return source.getValue() + "->" + type.getValue() + "->" + target.getValue();
    }

    public static KnowledgeEdge create(@NonNull NodeIdentifier source, @NonNull NodeIdentifier target, @NonNull RelationshipType type) {
        String edgeId = generateId(source, target, type);
        return KnowledgeEdge.builder()
                .id(edgeId)
                .sourceNodeId(source)
                .targetNodeId(target)
                .relationshipType(type)
                .strength(RelationshipStrength.MEDIUM)
                .metadata(new EdgeMetadata())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static KnowledgeEdge create(@NonNull NodeIdentifier source, @NonNull NodeIdentifier target, @NonNull RelationshipType type, double strength) {
        String edgeId = generateId(source, target, type);
        return KnowledgeEdge.builder()
                .id(edgeId)
                .sourceNodeId(source)
                .targetNodeId(target)
                .relationshipType(type)
                .strength(RelationshipStrength.of(strength))
                .metadata(new EdgeMetadata())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Override
    public String toString() {
        return "KnowledgeEdge{" +
                "id='" + id + '\'' +
                ", source=" + sourceNodeId +
                ", target=" + targetNodeId +
                ", type=" + relationshipType +
                ", strength=" + strength +
                ", bidirectional=" + bidirectional +
                '}';
    }
}
