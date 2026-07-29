package com.campusguide.personal.ai.atlas.knowledge.retrieval.collection;

import com.campusguide.personal.ai.atlas.context.query.QueryContext;
import com.campusguide.personal.ai.atlas.context.query.QueryDomain;
import com.campusguide.personal.ai.atlas.context.query.QueryIntent;
import com.campusguide.personal.ai.atlas.knowledge.collection.KnowledgeCollection;
import com.campusguide.personal.ai.atlas.knowledge.collection.KnowledgeCollectionRegistry;
import com.campusguide.personal.ai.atlas.knowledge.collection.KnowledgeCollectionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Intelligent collection selector that inspects QueryContext and determines which KnowledgeCollections
 * participate in semantic and hybrid retrieval, applying weights and fallback policies.
 */
@Component
@Slf4j
public class CollectionSelector {

    private final KnowledgeCollectionRegistry collectionRegistry;

    @Autowired
    public CollectionSelector(KnowledgeCollectionRegistry collectionRegistry) {
        this.collectionRegistry = collectionRegistry;
    }

    public List<KnowledgeCollectionSelection> selectCollections(
            QueryContext queryContext,
            String userId,
            List<String> userRoles,
            CollectionRetrievalPolicy policy,
            CollectionFilter customFilter) {

        if (policy == null) {
            policy = CollectionRetrievalPolicy.builder().build();
        }

        List<KnowledgeCollection> candidateCollections = collectionRegistry.findAccessibleCollections(userId, userRoles);

        Map<String, Double> scoredCollections = new HashMap<>();

        for (KnowledgeCollection collection : candidateCollections) {
            // Apply custom filter if provided
            if (customFilter != null && !customFilter.matches(collection)) {
                continue;
            }

            double basePriority = collection.getMetadata() != null ? collection.getMetadata().getPriority() : 1.0;
            double weight = policy.getWeightForCollection(collection.getCollectionId(), basePriority);

            double queryRelevance = calculateQueryRelevance(queryContext, collection);
            double totalScore = weight * (0.5 + queryRelevance);

            scoredCollections.put(collection.getCollectionId(), totalScore);
        }

        // Check fallback policy if no collections qualified or score threshold not met
        if (scoredCollections.isEmpty() && policy.isFallbackEnabled()) {
            log.info("No collections matched criteria. Applying fallback retrieval collections.");
            for (String fallbackId : policy.getFallbackCollectionIds()) {
                collectionRegistry.getCollection(fallbackId).ifPresent(col -> {
                    if (col.isAccessible(userId, userRoles)) {
                        scoredCollections.put(col.getCollectionId(), 0.5);
                    }
                });
            }
        }

        // Sort selected collections by total score descending
        List<KnowledgeCollectionSelection> selections = scoredCollections.entrySet().stream()
                .map(entry -> {
                    KnowledgeCollection col = collectionRegistry.getCollection(entry.getKey()).orElseThrow();
                    return new KnowledgeCollectionSelection(col, entry.getValue());
                })
                .sorted((a, b) -> Double.compare(b.weight(), a.weight()))
                .limit(policy.getMaxCollectionsToSearch())
                .toList();

        log.debug("Selected {} collections for query (top collection: {})",
                selections.size(),
                selections.isEmpty() ? "none" : selections.get(0).collection().getCollectionId());

        return selections;
    }

    private double calculateQueryRelevance(QueryContext queryContext, KnowledgeCollection collection) {
        if (queryContext == null) return 0.5;

        double relevance = 0.5;

        QueryIntent intent = queryContext.getIntent();
        QueryDomain domain = queryContext.getDomainClassification();

        if (intent == QueryIntent.ACADEMIC_INQUIRY && collection.getType() == KnowledgeCollectionType.ACADEMIC) {
            relevance += 0.4;
        } else if (domain == QueryDomain.ACADEMIC && (collection.getType() == KnowledgeCollectionType.ACADEMIC || collection.getType() == KnowledgeCollectionType.DEPARTMENTAL)) {
            relevance += 0.3;
        }

        if ((intent == QueryIntent.CAMPUS_NAVIGATION || domain == QueryDomain.CAMPUS) && collection.getType() == KnowledgeCollectionType.PUBLIC_KNOWLEDGE) {
            relevance += 0.4;
        }

        if ((intent == QueryIntent.USER_PROFILE || intent == QueryIntent.PLANNER_LOOKUP || domain == QueryDomain.USER) &&
                (collection.getType() == KnowledgeCollectionType.USER_MEMORY || collection.getType() == KnowledgeCollectionType.PRIVATE_USER)) {
            relevance += 0.4;
        }

        return Math.min(1.0, relevance);
    }

    public record KnowledgeCollectionSelection(KnowledgeCollection collection, double weight) {}
}
