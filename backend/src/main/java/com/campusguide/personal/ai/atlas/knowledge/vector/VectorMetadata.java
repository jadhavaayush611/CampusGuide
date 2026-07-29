package com.campusguide.personal.ai.atlas.knowledge.vector;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Filterable metadata associated with a VectorRecord.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VectorMetadata implements Serializable {

    private static final long serialVersionUID = 1L;

    private String documentId;
    private String collectionId;
    private String sourceType;
    private String category;
    private String domain;
    private Double score;
    private Double distance;
    @Builder.Default
    private Map<String, Object> fields = new HashMap<>();

    public void put(String key, Object value) {
        if (fields == null) {
            fields = new HashMap<>();
        }
        fields.put(key, value);
    }

    public Object get(String key) {
        return fields != null ? fields.get(key) : null;
    }

    public boolean matchesFilter(VectorMetadata filter) {
        if (filter == null) return true;

        if (filter.getDocumentId() != null && !filter.getDocumentId().equalsIgnoreCase(this.documentId)) {
            return false;
        }
        if (filter.getCollectionId() != null && !filter.getCollectionId().equalsIgnoreCase(this.collectionId)) {
            return false;
        }
        if (filter.getSourceType() != null && !filter.getSourceType().equalsIgnoreCase(this.sourceType)) {
            return false;
        }
        if (filter.getCategory() != null && !filter.getCategory().equalsIgnoreCase(this.category)) {
            return false;
        }
        if (filter.getDomain() != null && !filter.getDomain().equalsIgnoreCase(this.domain)) {
            return false;
        }

        if (filter.getFields() != null && !filter.getFields().isEmpty()) {
            if (this.fields == null) return false;
            for (Map.Entry<String, Object> entry : filter.getFields().entrySet()) {
                Object val = this.fields.get(entry.getKey());
                if (val == null || !val.toString().equalsIgnoreCase(entry.getValue().toString())) {
                    return false;
                }
            }
        }
        return true;
    }
}
