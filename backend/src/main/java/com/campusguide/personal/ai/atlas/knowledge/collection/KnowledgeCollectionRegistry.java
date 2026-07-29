package com.campusguide.personal.ai.atlas.knowledge.collection;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe registry for managing KnowledgeCollections across Atlas.
 * Provides lookup, access filtering, lifecycle tracking, and default collection initialization.
 */
@Component
@Slf4j
public class KnowledgeCollectionRegistry {

    public static final String DEFAULT_COLLECTION_ID = "default_collection";
    public static final String PUBLIC_CAMPUS_KNOWLEDGE = "public_campus_knowledge";
    public static final String ACADEMIC_CATALOG = "academic_catalog";
    public static final String DEPARTMENT_DOCS = "department_docs";
    public static final String USER_MEMORIES = "user_memories";

    private final Map<String, KnowledgeCollection> collectionMap = new ConcurrentHashMap<>();

    public KnowledgeCollectionRegistry() {
        initDefaultCollections();
    }

    private void initDefaultCollections() {
        registerCollection(KnowledgeCollection.builder()
                .collectionId(DEFAULT_COLLECTION_ID)
                .name("Default General Knowledge Collection")
                .description("Default system collection for unassigned artifacts")
                .type(KnowledgeCollectionType.SYSTEM)
                .scope(KnowledgeCollectionScope.GLOBAL)
                .lifecycleState(CollectionLifecycleState.ACTIVE)
                .version("1.0.0")
                .metadata(KnowledgeCollectionMetadata.builder()
                        .isPublic(true)
                        .priority(1.0)
                        .category("general")
                        .domain("campus")
                        .build())
                .build());

        registerCollection(KnowledgeCollection.builder()
                .collectionId(PUBLIC_CAMPUS_KNOWLEDGE)
                .name("Public Campus Knowledge")
                .description("Publicly accessible campus guides, FAQs, and facility information")
                .type(KnowledgeCollectionType.PUBLIC_KNOWLEDGE)
                .scope(KnowledgeCollectionScope.PUBLIC)
                .lifecycleState(CollectionLifecycleState.ACTIVE)
                .version("1.0.0")
                .metadata(KnowledgeCollectionMetadata.builder()
                        .isPublic(true)
                        .priority(1.2)
                        .category("campus_info")
                        .domain("campus")
                        .build())
                .build());

        registerCollection(KnowledgeCollection.builder()
                .collectionId(ACADEMIC_CATALOG)
                .name("Academic Catalog & Courses")
                .description("Official academic policies, course syllabi, and degree requirements")
                .type(KnowledgeCollectionType.ACADEMIC)
                .scope(KnowledgeCollectionScope.GLOBAL)
                .lifecycleState(CollectionLifecycleState.ACTIVE)
                .version("1.0.0")
                .metadata(KnowledgeCollectionMetadata.builder()
                        .isPublic(true)
                        .priority(1.1)
                        .category("academic")
                        .domain("academic")
                        .build())
                .build());

        registerCollection(KnowledgeCollection.builder()
                .collectionId(DEPARTMENT_DOCS)
                .name("Departmental Documentation")
                .description("Department specific resources, lab procedures, and faculty guides")
                .type(KnowledgeCollectionType.DEPARTMENTAL)
                .scope(KnowledgeCollectionScope.DEPARTMENT)
                .lifecycleState(CollectionLifecycleState.ACTIVE)
                .version("1.0.0")
                .metadata(KnowledgeCollectionMetadata.builder()
                        .isPublic(false)
                        .allowedRoles(Set.of("STUDENT", "FACULTY", "STAFF", "ADMIN"))
                        .priority(1.0)
                        .category("department")
                        .domain("academic")
                        .build())
                .build());

        registerCollection(KnowledgeCollection.builder()
                .collectionId(USER_MEMORIES)
                .name("User Memory & Personal Context")
                .description("User-scoped memory collections and personal saved preferences")
                .type(KnowledgeCollectionType.USER_MEMORY)
                .scope(KnowledgeCollectionScope.USER)
                .lifecycleState(CollectionLifecycleState.ACTIVE)
                .version("1.0.0")
                .metadata(KnowledgeCollectionMetadata.builder()
                        .isPublic(false)
                        .priority(1.5)
                        .category("user_memory")
                        .domain("personal")
                        .build())
                .build());
    }

    public void registerCollection(KnowledgeCollection collection) {
        if (collection == null || collection.getCollectionId() == null) {
            throw new IllegalArgumentException("Collection and collectionId must not be null");
        }
        collectionMap.put(collection.getCollectionId(), collection);
        log.info("Registered KnowledgeCollection: id={}, name={}, type={}, scope={}",
                collection.getCollectionId(), collection.getName(), collection.getType(), collection.getScope());
    }

    public Optional<KnowledgeCollection> getCollection(String collectionId) {
        if (collectionId == null) return Optional.empty();
        return Optional.ofNullable(collectionMap.get(collectionId));
    }

    public List<KnowledgeCollection> getAllCollections() {
        return new ArrayList<>(collectionMap.values());
    }

    public List<KnowledgeCollection> findAccessibleCollections(String userId, List<String> userRoles) {
        return collectionMap.values().stream()
                .filter(col -> col.isAccessible(userId, userRoles))
                .toList();
    }

    public List<KnowledgeCollection> findCollectionsByType(KnowledgeCollectionType type) {
        if (type == null) return List.of();
        return collectionMap.values().stream()
                .filter(col -> col.getType() == type)
                .toList();
    }

    public void updateLifecycle(String collectionId, CollectionLifecycleState newState, String reason) {
        getCollection(collectionId).ifPresent(col -> col.updateLifecycle(newState, reason));
    }

    public boolean unregisterCollection(String collectionId) {
        if (collectionId == null) return false;
        return collectionMap.remove(collectionId) != null;
    }

    public int count() {
        return collectionMap.size();
    }

    public void clear() {
        collectionMap.clear();
        initDefaultCollections();
    }
}
