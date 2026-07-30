package com.campusguide.personal.ai.atlas.knowledge.graph.node;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Core node entity in the Atlas Knowledge Graph.
 * Provider-independent representation of knowledge concepts, artifacts, people, courses, etc.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeNode implements Serializable {

    private static final long serialVersionUID = 1L;

    @NonNull
    private NodeIdentifier id;

    @NonNull
    @Builder.Default
    private NodeType type = NodeType.KNOWLEDGE_ARTIFACT;

    @NonNull
    @Builder.Default
    private String name = "";

    @Builder.Default
    private NodeAttributes attributes = new NodeAttributes();

    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    private String sourceCollectionId;

    private String sourceArtifactId;

    @Builder.Default
    private String provenance = "system";

    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private Instant updatedAt = Instant.now();

    public void updateAttributes(NodeAttributes newAttributes) {
        if (this.attributes == null) {
            this.attributes = new NodeAttributes();
        }
        if (newAttributes != null) {
            this.attributes.merge(newAttributes);
        }
        this.updatedAt = Instant.now();
    }

    public void addMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
        this.updatedAt = Instant.now();
    }

    /**
     * Sanitized string representation preventing raw content leaks in logs.
     */
    @Override
    public String toString() {
        return "KnowledgeNode{" +
                "id=" + id +
                ", type=" + type +
                ", name='" + name + '\'' +
                ", sourceCollectionId='" + sourceCollectionId + '\'' +
                ", sourceArtifactId='" + sourceArtifactId + '\'' +
                ", attributesCount=" + (attributes != null ? attributes.size() : 0) +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
