package com.campusguide.personal.ai.atlas.knowledge.collection;

/**
 * Tracks the lifecycle status of a KnowledgeCollection.
 */
public enum CollectionLifecycleState {
    DISCOVERED,
    INDEXING,
    ACTIVE,
    UPDATING,
    ARCHIVED,
    FAILED
}
