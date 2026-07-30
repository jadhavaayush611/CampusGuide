package com.campusguide.personal.ai.atlas.planning.graph;

/**
 * Type of dependency between two planning tasks.
 */
public enum DependencyType {
    HARD,
    SOFT,
    CONDITIONAL,
    TEMPORAL
}
