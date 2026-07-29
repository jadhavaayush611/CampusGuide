package com.campusguide.personal.ai.atlas.knowledge.catalog;

import com.campusguide.personal.ai.atlas.knowledge.artifact.ArtifactLifecycleState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service managing catalog inspection, metrics aggregation, entry tracking, and status transitions.
 */
@Service
public class KnowledgeCatalog {

    private final KnowledgeRegistry registry;

    @Autowired
    public KnowledgeCatalog(KnowledgeRegistry registry) {
        this.registry = registry != null ? registry : new KnowledgeRegistry();
    }

    public void registerEntry(KnowledgeCatalogEntry entry) {
        registry.register(entry);
    }

    public Optional<KnowledgeCatalogEntry> getEntry(String documentId) {
        return registry.findById(documentId);
    }

    public Optional<KnowledgeCatalogEntry> findByChecksum(String checksum) {
        return registry.findByChecksum(checksum);
    }

    public Optional<KnowledgeCatalogEntry> findByUri(String uri) {
        return registry.findByUri(uri);
    }

    public List<KnowledgeCatalogEntry> getAllEntries() {
        return registry.findAll();
    }

    public void updateStatus(String documentId, ArtifactLifecycleState status, String reason) {
        registry.findById(documentId).ifPresent(entry -> entry.transitionState(status, reason));
    }

    public CatalogSummary getSummary() {
        List<KnowledgeCatalogEntry> all = registry.findAll();
        int totalDocs = all.size();
        int totalChunks = all.stream().mapToInt(KnowledgeCatalogEntry::getTotalChunks).sum();
        int totalTokens = all.stream().mapToInt(KnowledgeCatalogEntry::getTotalTokens).sum();

        Map<ArtifactLifecycleState, Long> countsByStatus = all.stream()
                .collect(Collectors.groupingBy(KnowledgeCatalogEntry::getStatus, Collectors.counting()));

        return new CatalogSummary(totalDocs, totalChunks, totalTokens, countsByStatus);
    }

    public record CatalogSummary(
            int totalDocuments,
            int totalChunks,
            int totalTokens,
            Map<ArtifactLifecycleState, Long> countsByStatus
    ) {}
}
