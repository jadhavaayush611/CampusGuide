package com.campusguide.personal.ai.atlas.knowledge.retrieval.semantic;

import com.campusguide.personal.ai.atlas.knowledge.artifact.KnowledgeArtifact;
import com.campusguide.personal.ai.atlas.knowledge.vector.VectorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * High-level component for retrieving full KnowledgeArtifact payloads by identifier or collection bounds.
 */
@Component
@Slf4j
public class ArtifactRetriever {

    private final VectorRepository vectorRepository;

    @Autowired
    public ArtifactRetriever(VectorRepository vectorRepository) {
        this.vectorRepository = vectorRepository;
    }

    public Optional<KnowledgeArtifact> getArtifactById(String artifactId) {
        if (artifactId == null || artifactId.isBlank()) return Optional.empty();
        return vectorRepository.findByArtifactId(artifactId);
    }

    public List<KnowledgeArtifact> getArtifactsByIds(List<String> artifactIds) {
        if (artifactIds == null || artifactIds.isEmpty()) return List.of();
        List<KnowledgeArtifact> results = new ArrayList<>();
        for (String id : artifactIds) {
            getArtifactById(id).ifPresent(results::add);
        }
        return results;
    }
}
