package com.campusguide.personal.ai.atlas.knowledge.graph.edge;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Standard relationship types between nodes in the Knowledge Graph.
 * Supports extension via string mapping.
 */
public enum RelationshipType {

    BELONGS_TO("BELONGS_TO"),
    PART_OF("PART_OF"),
    LOCATED_IN("LOCATED_IN"),
    TEACHES("TEACHES"),
    ENROLLED_IN("ENROLLED_IN"),
    REFERENCES("REFERENCES"),
    DEPENDS_ON("DEPENDS_ON"),
    RELATED_TO("RELATED_TO"),
    PREREQUISITE("PREREQUISITE"),
    NEXT("NEXT"),
    PREVIOUS("PREVIOUS"),
    USES("USES"),
    CONTAINS("CONTAINS"),
    SAME_DEPARTMENT("SAME_DEPARTMENT"),
    REQUIRES("REQUIRES"),
    PREREQUISITE_FOR("PREREQUISITE_FOR"),
    INFERRED_ACADEMIC_PEER("INFERRED_ACADEMIC_PEER"),
    INDIRECT_PREREQUISITE("INDIRECT_PREREQUISITE"),
    CO_LOCATED("CO_LOCATED"),
    TAUGHT_BY("TAUGHT_BY"),
    CUSTOM("CUSTOM");

    private final String value;

    RelationshipType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static RelationshipType fromString(String input) {
        if (input == null || input.isBlank()) {
            return RELATED_TO;
        }
        String normalized = input.trim().toUpperCase().replace("-", "_").replace(" ", "_");
        for (RelationshipType type : RelationshipType.values()) {
            if (type.name().equalsIgnoreCase(normalized) || type.value.equalsIgnoreCase(normalized)) {
                return type;
            }
        }
        return CUSTOM;
    }

    @Override
    public String toString() {
        return value;
    }
}
