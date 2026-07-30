package com.campusguide.personal.ai.atlas.knowledge.graph.lifecycle;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Lifecycle states of a KnowledgeGraph within Atlas.
 */
public enum GraphLifecycleState {

    DISCOVERED("Discovered"),
    BUILDING("Building"),
    ACTIVE("Active"),
    UPDATING("Updating"),
    FAILED("Failed"),
    ARCHIVED("Archived");

    private final String displayName;

    GraphLifecycleState(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static GraphLifecycleState fromString(String input) {
        if (input == null || input.isBlank()) {
            return DISCOVERED;
        }
        for (GraphLifecycleState state : GraphLifecycleState.values()) {
            if (state.name().equalsIgnoreCase(input) || state.displayName.equalsIgnoreCase(input)) {
                return state;
            }
        }
        return DISCOVERED;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
