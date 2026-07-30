package com.campusguide.personal.ai.atlas.knowledge.graph.extraction;

import com.campusguide.personal.ai.atlas.knowledge.artifact.KnowledgeArtifact;
import com.campusguide.personal.ai.atlas.knowledge.collection.KnowledgeCollection;
import com.campusguide.personal.ai.atlas.knowledge.graph.edge.KnowledgeEdge;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Registry managing registered RelationshipExtractor strategies and orchestrating edge extraction.
 */
@Component
public class RelationshipRegistry {

    private final List<RelationshipExtractor> extractors = new CopyOnWriteArrayList<>();

    public RelationshipRegistry(List<RelationshipExtractor> defaultExtractors) {
        if (defaultExtractors != null) {
            this.extractors.addAll(defaultExtractors);
            sortExtractors();
        }
    }

    public void registerExtractor(RelationshipExtractor extractor) {
        if (extractor != null) {
            this.extractors.add(extractor);
            sortExtractors();
        }
    }

    public void unregisterExtractor(String extractorName) {
        this.extractors.removeIf(e -> e.getName().equalsIgnoreCase(extractorName));
    }

    public List<KnowledgeEdge> extractFromArtifact(KnowledgeArtifact artifact) {
        List<KnowledgeEdge> results = new ArrayList<>();
        if (artifact == null) return results;

        for (RelationshipExtractor extractor : extractors) {
            try {
                List<KnowledgeEdge> extracted = extractor.extractFromArtifact(artifact);
                if (extracted != null) {
                    results.addAll(extracted);
                }
            } catch (Exception e) {
                // Defensive exception swallowing for extractor resilience
            }
        }
        return results;
    }

    public List<KnowledgeEdge> extractFromCollection(KnowledgeCollection collection, List<KnowledgeArtifact> artifacts) {
        List<KnowledgeEdge> results = new ArrayList<>();
        if (collection == null && (artifacts == null || artifacts.isEmpty())) return results;

        for (RelationshipExtractor extractor : extractors) {
            try {
                List<KnowledgeEdge> extracted = extractor.extractFromCollection(collection, artifacts);
                if (extracted != null) {
                    results.addAll(extracted);
                }
            } catch (Exception e) {
                // Defensive exception swallowing for extractor resilience
            }
        }
        return results;
    }

    public List<RelationshipExtractor> getExtractors() {
        return new ArrayList<>(extractors);
    }

    private void sortExtractors() {
        this.extractors.sort(Comparator.comparingInt(RelationshipExtractor::getPriority));
    }
}
