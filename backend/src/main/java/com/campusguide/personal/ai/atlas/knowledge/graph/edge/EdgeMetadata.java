package com.campusguide.personal.ai.atlas.knowledge.graph.edge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Metadata associated with a KnowledgeEdge including extraction provenance and custom properties.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EdgeMetadata implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String extractorName = "rule-based";

    @Builder.Default
    private String provenance = "inferred";

    private String sourceArtifactId;

    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private Instant updatedAt = Instant.now();

    @Builder.Default
    private Map<String, Object> properties = new HashMap<>();

    public void addProperty(String key, Object value) {
        if (this.properties == null) {
            this.properties = new HashMap<>();
        }
        this.properties.put(key, value);
        this.updatedAt = Instant.now();
    }

    public Object getProperty(String key) {
        return this.properties != null ? this.properties.get(key) : null;
    }
}
