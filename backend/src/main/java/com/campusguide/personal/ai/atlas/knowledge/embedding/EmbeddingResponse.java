package com.campusguide.personal.ai.atlas.knowledge.embedding;

import com.campusguide.personal.ai.atlas.knowledge.artifact.ArtifactEmbedding;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates batch embedding responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddingResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private List<ArtifactEmbedding> embeddings = new ArrayList<>();
    private String provider;
    private String model;
    private int totalTokens;
    private long durationMs;
}
