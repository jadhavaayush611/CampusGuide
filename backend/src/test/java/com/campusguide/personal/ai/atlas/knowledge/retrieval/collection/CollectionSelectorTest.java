package com.campusguide.personal.ai.atlas.knowledge.retrieval.collection;

import com.campusguide.personal.ai.atlas.context.query.QueryContext;
import com.campusguide.personal.ai.atlas.context.query.QueryDomain;
import com.campusguide.personal.ai.atlas.context.query.QueryIntent;
import com.campusguide.personal.ai.atlas.knowledge.collection.KnowledgeCollectionRegistry;
import com.campusguide.personal.ai.atlas.knowledge.retrieval.collection.CollectionSelector.KnowledgeCollectionSelection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CollectionSelectorTest {

    private KnowledgeCollectionRegistry registry;
    private CollectionSelector collectionSelector;

    @BeforeEach
    void setUp() {
        registry = new KnowledgeCollectionRegistry();
        collectionSelector = new CollectionSelector(registry);
    }

    @Test
    @DisplayName("Should select academic collection for ACADEMIC_INQUIRY query intent")
    void testSelectAcademicCollectionForAcademicQuery() {
        QueryContext qc = QueryContext.builder()
                .rawQuery("What are the prerequisites for CS 101?")
                .intent(QueryIntent.ACADEMIC_INQUIRY)
                .domainClassification(QueryDomain.ACADEMIC)
                .confidenceScore(0.9)
                .build();

        CollectionRetrievalPolicy policy = CollectionRetrievalPolicy.builder().build();

        List<KnowledgeCollectionSelection> selections = collectionSelector.selectCollections(
                qc, "user_1", List.of("STUDENT"), policy, null);

        assertFalse(selections.isEmpty());
        assertTrue(selections.stream().anyMatch(s -> s.collection().getCollectionId().equals(KnowledgeCollectionRegistry.ACADEMIC_CATALOG)));
    }

    @Test
    @DisplayName("Should apply fallback collections when custom filter excludes all candidates")
    void testFallbackCollectionsOnEmptyMatch() {
        QueryContext qc = QueryContext.builder()
                .rawQuery("Where is the library located?")
                .intent(QueryIntent.CAMPUS_NAVIGATION)
                .confidenceScore(0.8)
                .build();

        CollectionFilter filter = CollectionFilter.builder()
                .targetCollectionIds(java.util.Set.of("non_existent_collection"))
                .build();

        CollectionRetrievalPolicy policy = CollectionRetrievalPolicy.builder()
                .fallbackEnabled(true)
                .build();

        List<KnowledgeCollectionSelection> selections = collectionSelector.selectCollections(
                qc, "user_1", List.of("STUDENT"), policy, filter);

        assertFalse(selections.isEmpty());
        assertTrue(selections.stream().anyMatch(s -> s.collection().getCollectionId().equals(KnowledgeCollectionRegistry.PUBLIC_CAMPUS_KNOWLEDGE)));
    }
}
