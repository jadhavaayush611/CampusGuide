package com.campusguide.personal.ai.atlas.knowledge.collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class KnowledgeCollectionRegistryTest {

    private KnowledgeCollectionRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new KnowledgeCollectionRegistry();
    }

    @Test
    @DisplayName("Should initialize with default system collections")
    void testDefaultCollectionsInitialization() {
        assertTrue(registry.count() >= 5);
        assertTrue(registry.getCollection(KnowledgeCollectionRegistry.DEFAULT_COLLECTION_ID).isPresent());
        assertTrue(registry.getCollection(KnowledgeCollectionRegistry.PUBLIC_CAMPUS_KNOWLEDGE).isPresent());
        assertTrue(registry.getCollection(KnowledgeCollectionRegistry.ACADEMIC_CATALOG).isPresent());
    }

    @Test
    @DisplayName("Should register and find accessible collections for user and roles")
    void testRegisterAndFindAccessibleCollections() {
        KnowledgeCollection custom = KnowledgeCollection.builder()
                .collectionId("dept_labs")
                .name("Engineering Labs")
                .type(KnowledgeCollectionType.DEPARTMENTAL)
                .scope(KnowledgeCollectionScope.DEPARTMENT)
                .lifecycleState(CollectionLifecycleState.ACTIVE)
                .metadata(KnowledgeCollectionMetadata.builder()
                        .isPublic(false)
                        .allowedRoles(java.util.Set.of("ENGINEERING"))
                        .build())
                .build();

        registry.registerCollection(custom);

        Optional<KnowledgeCollection> found = registry.getCollection("dept_labs");
        assertTrue(found.isPresent());
        assertEquals("Engineering Labs", found.get().getName());

        List<KnowledgeCollection> accessibleForEng = registry.findAccessibleCollections("user_eng", List.of("ENGINEERING"));
        assertTrue(accessibleForEng.stream().anyMatch(c -> c.getCollectionId().equals("dept_labs")));

        List<KnowledgeCollection> accessibleForMath = registry.findAccessibleCollections("user_math", List.of("MATH"));
        assertFalse(accessibleForMath.stream().anyMatch(c -> c.getCollectionId().equals("dept_labs")));
    }
}
