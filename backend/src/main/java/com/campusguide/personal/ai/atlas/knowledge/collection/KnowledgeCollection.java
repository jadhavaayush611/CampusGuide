package com.campusguide.personal.ai.atlas.knowledge.collection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Core KnowledgeCollection boundary model.
 * In Atlas Phase 3.3, KnowledgeCollections act as the provider-independent retrieval boundary.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeCollection implements Serializable {

    private static final long serialVersionUID = 1L;

    private String collectionId;
    private String name;
    private String description;

    @Builder.Default
    private KnowledgeCollectionType type = KnowledgeCollectionType.PUBLIC_KNOWLEDGE;

    @Builder.Default
    private KnowledgeCollectionScope scope = KnowledgeCollectionScope.PUBLIC;

    @Builder.Default
    private CollectionLifecycleState lifecycleState = CollectionLifecycleState.DISCOVERED;

    @Builder.Default
    private String version = "1.0.0";

    @Builder.Default
    private KnowledgeCollectionMetadata metadata = new KnowledgeCollectionMetadata();

    @Builder.Default
    private KnowledgeCollectionStatistics statistics = new KnowledgeCollectionStatistics();

    @Builder.Default
    private List<CollectionUpdateRecord> updateHistory = new ArrayList<>();

    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private Instant updatedAt = Instant.now();

    public boolean isAccessible(String userId, List<String> userRoles) {
        if (lifecycleState == CollectionLifecycleState.ARCHIVED || lifecycleState == CollectionLifecycleState.FAILED) {
            return false;
        }

        return switch (scope) {
            case GLOBAL, SYSTEM, PUBLIC -> true;
            case PRIVATE, USER -> userId != null && metadata != null && userId.equals(metadata.getOwnerId());
            case DEPARTMENT, FACULTY, ROLE_BASED -> metadata != null && metadata.isUserAuthorized(userId, userRoles);
        };
    }

    public void updateLifecycle(CollectionLifecycleState newState, String reason) {
        this.lifecycleState = newState;
        this.updatedAt = Instant.now();
        if (this.updateHistory == null) {
            this.updateHistory = new ArrayList<>();
        }
        this.updateHistory.add(CollectionUpdateRecord.builder()
                .version(this.version)
                .timestamp(Instant.now())
                .description("Lifecycle state transition to " + newState + (reason != null ? ": " + reason : ""))
                .updatedBy("SYSTEM")
                .state(newState)
                .build());
    }

    public void recordUpdate(String newVersion, String description, String updatedBy) {
        if (newVersion != null) {
            this.version = newVersion;
        }
        this.updatedAt = Instant.now();
        if (this.statistics != null) {
            this.statistics.setLastUpdatedAt(this.updatedAt);
        }
        if (this.updateHistory == null) {
            this.updateHistory = new ArrayList<>();
        }
        this.updateHistory.add(CollectionUpdateRecord.builder()
                .version(this.version)
                .timestamp(Instant.now())
                .description(description != null ? description : "Collection updated")
                .updatedBy(updatedBy != null ? updatedBy : "SYSTEM")
                .state(this.lifecycleState)
                .build());
    }
}
