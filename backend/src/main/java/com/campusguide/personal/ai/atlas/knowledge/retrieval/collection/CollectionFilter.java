package com.campusguide.personal.ai.atlas.knowledge.retrieval.collection;

import com.campusguide.personal.ai.atlas.knowledge.collection.KnowledgeCollection;
import com.campusguide.personal.ai.atlas.knowledge.collection.KnowledgeCollectionScope;
import com.campusguide.personal.ai.atlas.knowledge.collection.KnowledgeCollectionType;
import com.campusguide.personal.ai.atlas.knowledge.vector.VectorMetadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.*;

/**
 * Filter predicate used to prune KnowledgeCollections and scope retrieval queries.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionFilter implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private Set<String> targetCollectionIds = new HashSet<>();

    @Builder.Default
    private Set<KnowledgeCollectionType> allowedTypes = new HashSet<>();

    @Builder.Default
    private Set<KnowledgeCollectionScope> allowedScopes = new HashSet<>();

    private String ownerId;

    @Builder.Default
    private List<String> userRoles = new ArrayList<>();

    @Builder.Default
    private double minPriority = 0.0;

    public boolean matches(KnowledgeCollection collection) {
        if (collection == null) return false;

        // Check explicit target collections if specified
        if (targetCollectionIds != null && !targetCollectionIds.isEmpty() &&
                !targetCollectionIds.contains(collection.getCollectionId())) {
            return false;
        }

        // Check collection types if specified
        if (allowedTypes != null && !allowedTypes.isEmpty() &&
                !allowedTypes.contains(collection.getType())) {
            return false;
        }

        // Check collection scopes if specified
        if (allowedScopes != null && !allowedScopes.isEmpty() &&
                !allowedScopes.contains(collection.getScope())) {
            return false;
        }

        // Check priority threshold
        if (collection.getMetadata() != null && collection.getMetadata().getPriority() < minPriority) {
            return false;
        }

        // Check collection accessibility permissions
        return collection.isAccessible(ownerId, userRoles);
    }

    public VectorMetadata toVectorMetadataFilter(String collectionId) {
        VectorMetadata vm = new VectorMetadata();
        vm.setCollectionId(collectionId);
        return vm;
    }
}
