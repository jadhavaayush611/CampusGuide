package com.campusguide.personal.ai.atlas.knowledge.artifact;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import java.io.Serializable;
import java.util.UUID;

/**
 * Universal unique identifier wrapper for a KnowledgeArtifact.
 */
@Getter
@EqualsAndHashCode
public class ArtifactIdentifier implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String value;

    public ArtifactIdentifier(@NonNull String value) {
        if (value.isBlank()) {
            throw new IllegalArgumentException("ArtifactIdentifier value cannot be blank");
        }
        this.value = value;
    }

    public static ArtifactIdentifier generate() {
        return new ArtifactIdentifier("art_" + UUID.randomUUID().toString().replace("-", ""));
    }

    public static ArtifactIdentifier of(String value) {
        return new ArtifactIdentifier(value);
    }

    public static ArtifactIdentifier generateChunkId(ArtifactIdentifier parentId, int chunkIndex) {
        return new ArtifactIdentifier(parentId.getValue() + "_chk_" + chunkIndex);
    }

    @Override
    public String toString() {
        return value;
    }
}
