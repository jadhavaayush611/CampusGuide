package com.campusguide.personal.ai.atlas.knowledge.artifact;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Core universal provider-independent KnowledgeArtifact model.
 * All knowledge and retrieval sources in Atlas operate exclusively on KnowledgeArtifact.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeArtifact implements Serializable {

    private static final long serialVersionUID = 1L;

    @NonNull
    private ArtifactIdentifier id;

    @NonNull
    @Builder.Default
    private String collectionId = "default_collection";

    @NonNull
    @Builder.Default
    private ArtifactType type = ArtifactType.DOCUMENT;

    private String content;

    @Builder.Default
    private ArtifactMetadata metadata = new ArtifactMetadata();

    @Builder.Default
    private ArtifactSource source = new ArtifactSource();

    @Builder.Default
    private ArtifactVersion version = new ArtifactVersion();

    @Builder.Default
    private List<ArtifactReference> references = new ArrayList<>();

    private ArtifactEmbedding embedding;

    @Builder.Default
    private ArtifactLifecycleState lifecycleState = ArtifactLifecycleState.DISCOVERED;

    @Builder.Default
    private Map<String, Object> retrievalHints = new HashMap<>();

    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private Instant updatedAt = Instant.now();

    public ArtifactVersion getVersion() {
        if (this.version == null || this.version.getChecksum() == null) {
            this.version = ArtifactVersion.initial(this.content != null ? this.content : "");
        }
        return this.version;
    }

    public void addReference(ArtifactReference reference) {
        if (this.references == null) {
            this.references = new ArrayList<>();
        }
        this.references.add(reference);
    }

    public void addRetrievalHint(String key, Object value) {
        if (this.retrievalHints == null) {
            this.retrievalHints = new HashMap<>();
        }
        this.retrievalHints.put(key, value);
    }

    public void updateContent(String newContent) {
        this.content = newContent;
        this.version = ArtifactVersion.initial(newContent);
        this.updatedAt = Instant.now();
    }

    public boolean hasEmbedding() {
        return embedding != null && embedding.getVector() != null && embedding.getVector().length > 0;
    }

    public void updateLifecycle(ArtifactLifecycleState newState) {
        this.lifecycleState = newState;
        this.updatedAt = Instant.now();
    }
}
