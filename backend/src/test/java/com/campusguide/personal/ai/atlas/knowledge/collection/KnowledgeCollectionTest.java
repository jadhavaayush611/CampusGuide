package com.campusguide.personal.ai.atlas.knowledge.collection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class KnowledgeCollectionTest {

    @Test
    @DisplayName("Should create KnowledgeCollection with default properties and verify lifecycle state transitions")
    void testCollectionCreationAndLifecycle() {
        KnowledgeCollection collection = KnowledgeCollection.builder()
                .collectionId("col_cs_101")
                .name("Computer Science 101 Docs")
                .description("Introductory CS course material")
                .type(KnowledgeCollectionType.ACADEMIC)
                .scope(KnowledgeCollectionScope.DEPARTMENT)
                .metadata(KnowledgeCollectionMetadata.builder()
                        .isPublic(false)
                        .allowedRoles(Set.of("STUDENT", "FACULTY"))
                        .ownerId("dept_cs")
                        .priority(1.5)
                        .build())
                .build();

        assertEquals("col_cs_101", collection.getCollectionId());
        assertEquals(CollectionLifecycleState.DISCOVERED, collection.getLifecycleState());
        assertEquals("1.0.0", collection.getVersion());

        // Lifecycle transition
        collection.updateLifecycle(CollectionLifecycleState.INDEXING, "Starting vector indexing");
        assertEquals(CollectionLifecycleState.INDEXING, collection.getLifecycleState());
        assertEquals(1, collection.getUpdateHistory().size());

        collection.updateLifecycle(CollectionLifecycleState.ACTIVE, "Indexing complete");
        assertEquals(CollectionLifecycleState.ACTIVE, collection.getLifecycleState());
        assertEquals(2, collection.getUpdateHistory().size());
    }

    @Test
    @DisplayName("Should verify access control permissions for public, private, and role-based collections")
    void testAccessControlPermissions() {
        KnowledgeCollection publicCol = KnowledgeCollection.builder()
                .collectionId("public_col")
                .scope(KnowledgeCollectionScope.PUBLIC)
                .lifecycleState(CollectionLifecycleState.ACTIVE)
                .metadata(KnowledgeCollectionMetadata.builder().isPublic(true).build())
                .build();

        assertTrue(publicCol.isAccessible("any_user", List.of()));

        KnowledgeCollection privateCol = KnowledgeCollection.builder()
                .collectionId("user_col_1")
                .scope(KnowledgeCollectionScope.USER)
                .lifecycleState(CollectionLifecycleState.ACTIVE)
                .metadata(KnowledgeCollectionMetadata.builder()
                        .isPublic(false)
                        .ownerId("user_123")
                        .build())
                .build();

        assertTrue(privateCol.isAccessible("user_123", List.of()));
        assertFalse(privateCol.isAccessible("user_456", List.of()));

        KnowledgeCollection roleCol = KnowledgeCollection.builder()
                .collectionId("faculty_col")
                .scope(KnowledgeCollectionScope.FACULTY)
                .lifecycleState(CollectionLifecycleState.ACTIVE)
                .metadata(KnowledgeCollectionMetadata.builder()
                        .isPublic(false)
                        .allowedRoles(Set.of("FACULTY"))
                        .build())
                .build();

        assertTrue(roleCol.isAccessible("prof_smith", List.of("FACULTY")));
        assertFalse(roleCol.isAccessible("student_bob", List.of("STUDENT")));
    }

    @Test
    @DisplayName("Should track statistics accurately when artifacts are added")
    void testStatisticsTracking() {
        KnowledgeCollectionStatistics stats = new KnowledgeCollectionStatistics();
        assertEquals(0, stats.getTotalArtifactCount());

        stats.incrementArtifacts(5, 1024L);
        assertEquals(1, stats.getTotalArtifactCount());
        assertEquals(5, stats.getTotalChunkCount());
        assertEquals(5, stats.getTotalVectorCount());
        assertEquals(1024L, stats.getByteSize());
        assertNotNull(stats.getLastUpdatedAt());
    }
}
