package com.campusguide.personal.ai.atlas.knowledge.catalog;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe repository/registry maintaining KnowledgeCatalogEntry records.
 */
@Component
public class KnowledgeRegistry {

    private final Map<String, KnowledgeCatalogEntry> entriesById = new ConcurrentHashMap<>();
    private final Map<String, String> checksumToDocId = new ConcurrentHashMap<>();
    private final Map<String, String> uriToDocId = new ConcurrentHashMap<>();

    public void register(KnowledgeCatalogEntry entry) {
        if (entry == null || entry.getDocumentId() == null) return;
        entriesById.put(entry.getDocumentId(), entry);
        if (entry.getChecksum() != null && !entry.getChecksum().isBlank()) {
            checksumToDocId.put(entry.getChecksum(), entry.getDocumentId());
        }
        if (entry.getSourceUri() != null && !entry.getSourceUri().isBlank()) {
            uriToDocId.put(entry.getSourceUri(), entry.getDocumentId());
        }
    }

    public Optional<KnowledgeCatalogEntry> findById(String documentId) {
        if (documentId == null) return Optional.empty();
        return Optional.ofNullable(entriesById.get(documentId));
    }

    public Optional<KnowledgeCatalogEntry> findByChecksum(String checksum) {
        if (checksum == null) return Optional.empty();
        String docId = checksumToDocId.get(checksum);
        return docId != null ? findById(docId) : Optional.empty();
    }

    public Optional<KnowledgeCatalogEntry> findByUri(String sourceUri) {
        if (sourceUri == null) return Optional.empty();
        String docId = uriToDocId.get(sourceUri);
        return docId != null ? findById(docId) : Optional.empty();
    }

    public List<KnowledgeCatalogEntry> findAll() {
        return new ArrayList<>(entriesById.values());
    }

    public boolean remove(String documentId) {
        KnowledgeCatalogEntry entry = entriesById.remove(documentId);
        if (entry != null) {
            if (entry.getChecksum() != null) checksumToDocId.remove(entry.getChecksum());
            if (entry.getSourceUri() != null) uriToDocId.remove(entry.getSourceUri());
            return true;
        }
        return false;
    }

    public void clear() {
        entriesById.clear();
        checksumToDocId.clear();
        uriToDocId.clear();
    }

    public int count() {
        return entriesById.size();
    }
}
