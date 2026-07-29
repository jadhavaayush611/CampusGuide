package com.campusguide.personal.ai.atlas.knowledge.artifact;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Key-value metadata store associated with a KnowledgeArtifact.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtifactMetadata implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String category;
    private String domain;
    private String language;
    private Long sizeInBytes;
    @Builder.Default
    private Map<String, Object> attributes = new HashMap<>();

    public void put(String key, Object value) {
        if (this.attributes == null) {
            this.attributes = new HashMap<>();
        }
        this.attributes.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        if (attributes == null || !attributes.containsKey(key)) {
            return null;
        }
        Object obj = attributes.get(key);
        if (clazz.isInstance(obj)) {
            return (T) obj;
        }
        return null;
    }

    public Object getOrDefault(String key, Object defaultValue) {
        if (attributes == null || !attributes.containsKey(key)) {
            return defaultValue;
        }
        return attributes.get(key);
    }

    public boolean containsKey(String key) {
        return attributes != null && attributes.containsKey(key);
    }
}
