package com.campusguide.personal.ai.atlas.knowledge.retrieval.collection;

import com.campusguide.personal.ai.atlas.knowledge.collection.KnowledgeCollectionRegistry;
import com.campusguide.personal.ai.atlas.knowledge.collection.KnowledgeCollectionScope;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Defines policy constraints and fallback rules for collection-aware retrieval.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionRetrievalPolicy implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private KnowledgeCollectionScope defaultScope = KnowledgeCollectionScope.PUBLIC;

    @Builder.Default
    private boolean fallbackEnabled = true;

    @Builder.Default
    private List<String> fallbackCollectionIds = List.of(
            KnowledgeCollectionRegistry.PUBLIC_CAMPUS_KNOWLEDGE,
            KnowledgeCollectionRegistry.DEFAULT_COLLECTION_ID
    );

    @Builder.Default
    private boolean weightingEnabled = true;

    @Builder.Default
    private double minConfidenceThreshold = 0.30;

    @Builder.Default
    private int maxCollectionsToSearch = 5;

    @Builder.Default
    private Map<String, Double> collectionPriorityWeights = new HashMap<>();

    public double getWeightForCollection(String collectionId, double defaultPriority) {
        if (!weightingEnabled || collectionId == null) return defaultPriority;
        return collectionPriorityWeights.getOrDefault(collectionId, defaultPriority);
    }
}
