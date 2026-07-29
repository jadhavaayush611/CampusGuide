package com.campusguide.personal.ai.atlas.knowledge.catalog;

import com.campusguide.personal.ai.atlas.knowledge.artifact.ArtifactLifecycleState;
import com.campusguide.personal.ai.atlas.knowledge.artifact.ArtifactVersion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Catalog entry tracking indexing state, provenance, checksums, chunk counts, and diagnostics for a document.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeCatalogEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    private String documentId;
    private String sourceUri;
    private String sourceType;
    private String title;
    private String checksum;
    private ArtifactVersion version;

    @Builder.Default
    private ArtifactLifecycleState status = ArtifactLifecycleState.DISCOVERED;

    private int totalChunks;
    private int totalTokens;

    @Builder.Default
    private List<String> indexedArtifactIds = new ArrayList<>();

    @Builder.Default
    private List<String> diagnostics = new ArrayList<>();

    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private Instant updatedAt = Instant.now();

    public void addDiagnostic(String logEntry) {
        if (diagnostics == null) {
            diagnostics = new ArrayList<>();
        }
        diagnostics.add(Instant.now() + " - " + logEntry);
        this.updatedAt = Instant.now();
    }

    public void transitionState(ArtifactLifecycleState newState, String reason) {
        addDiagnostic("State transitioned from " + this.status + " to " + newState + (reason != null ? " (" + reason + ")" : ""));
        this.status = newState;
        this.updatedAt = Instant.now();
    }
}
