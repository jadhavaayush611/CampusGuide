package com.campusguide.personal.ai.atlas.knowledge.artifact;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Provenance tracking for KnowledgeArtifact.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtifactSource implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sourceUri;
    private String sourceType;
    private String title;
    private String author;
    private Integer pageNumber;
    private Integer lineStart;
    private Integer lineEnd;
    private Integer startOffset;
    private Integer endOffset;
    @Builder.Default
    private Instant creationTimestamp = Instant.now();
    @Builder.Default
    private Map<String, Object> attributes = new HashMap<>();

    public void addAttribute(String key, Object value) {
        if (this.attributes == null) {
            this.attributes = new HashMap<>();
        }
        this.attributes.put(key, value);
    }
}
