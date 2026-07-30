package com.campusguide.personal.ai.atlas.knowledge.graph.node;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * Supported types of entities modeled as nodes within the Atlas Knowledge Graph.
 */
public enum NodeType {

    KNOWLEDGE_ARTIFACT("KnowledgeArtifact"),
    KNOWLEDGE_COLLECTION("KnowledgeCollection"),
    PERSON("Person"),
    COURSE("Course"),
    BUILDING("Building"),
    DEPARTMENT("Department"),
    EVENT("Event"),
    SERVICE("Service"),
    DOCUMENT("Document"),
    STUDENT("Student"),
    FACULTY("Faculty"),
    CUSTOM("Custom");

    private final String displayName;

    NodeType(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static NodeType fromString(String value) {
        if (value == null || value.isBlank()) {
            return CUSTOM;
        }
        String normalized = value.trim().replace("_", "").replace("-", "");
        for (NodeType type : NodeType.values()) {
            if (type.name().equalsIgnoreCase(value) ||
                type.displayName.equalsIgnoreCase(value) ||
                type.name().replace("_", "").equalsIgnoreCase(normalized) ||
                type.displayName.replace(" ", "").equalsIgnoreCase(normalized)) {
                return type;
            }
        }
        return CUSTOM;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
