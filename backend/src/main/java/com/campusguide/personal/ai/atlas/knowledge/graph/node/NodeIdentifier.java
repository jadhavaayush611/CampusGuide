package com.campusguide.personal.ai.atlas.knowledge.graph.node;

import com.campusguide.personal.ai.atlas.knowledge.artifact.ArtifactIdentifier;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import java.io.Serializable;
import java.util.UUID;

/**
 * Unique identifier for a KnowledgeNode in the Knowledge Graph.
 */
@Getter
@EqualsAndHashCode
public class NodeIdentifier implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String value;

    public NodeIdentifier(@NonNull String value) {
        if (value.isBlank()) {
            throw new IllegalArgumentException("NodeIdentifier value cannot be blank");
        }
        this.value = value;
    }

    public static NodeIdentifier generate() {
        return new NodeIdentifier("node_" + UUID.randomUUID().toString().replace("-", ""));
    }

    public static NodeIdentifier of(@NonNull String value) {
        return new NodeIdentifier(value);
    }

    public static NodeIdentifier of(@NonNull NodeType type, @NonNull String id) {
        return new NodeIdentifier(type.name().toLowerCase() + ":" + id);
    }

    public static NodeIdentifier ofArtifact(@NonNull ArtifactIdentifier artifactId) {
        return new NodeIdentifier("artifact:" + artifactId.getValue());
    }

    public static NodeIdentifier ofCollection(@NonNull String collectionId) {
        return new NodeIdentifier("collection:" + collectionId);
    }

    @Override
    public String toString() {
        return value;
    }
}
