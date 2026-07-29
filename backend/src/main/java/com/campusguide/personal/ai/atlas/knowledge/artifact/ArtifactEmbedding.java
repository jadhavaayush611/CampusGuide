package com.campusguide.personal.ai.atlas.knowledge.artifact;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.Arrays;

/**
 * Encapsulates vector embedding representations of a KnowledgeArtifact.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtifactEmbedding implements Serializable {

    private static final long serialVersionUID = 1L;

    private float[] vector;
    private String provider;
    private String model;
    private int dimension;
    @Builder.Default
    private Instant createdAt = Instant.now();

    public static ArtifactEmbedding of(float[] vector, String provider, String model) {
        return ArtifactEmbedding.builder()
                .vector(vector)
                .provider(provider)
                .model(model)
                .dimension(vector != null ? vector.length : 0)
                .createdAt(Instant.now())
                .build();
    }

    @Override
    public String toString() {
        return "ArtifactEmbedding{" +
                "provider='" + provider + '\'' +
                ", model='" + model + '\'' +
                ", dimension=" + dimension +
                ", createdAt=" + createdAt +
                '}';
    }
}
