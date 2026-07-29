package com.campusguide.personal.ai.atlas.knowledge.embedding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Representation of an embedding model configuration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddingModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String provider;
    private int dimension;
    private int maxInputTokens;

    public static final EmbeddingModel TEXT_EMBEDDING_3_SMALL = EmbeddingModel.builder()
            .name("text-embedding-3-small")
            .provider("openai")
            .dimension(1536)
            .maxInputTokens(8191)
            .build();

    public static final EmbeddingModel TEXT_EMBEDDING_3_LARGE = EmbeddingModel.builder()
            .name("text-embedding-3-large")
            .provider("openai")
            .dimension(3072)
            .maxInputTokens(8191)
            .build();

    public static final EmbeddingModel MOCK_MODEL = EmbeddingModel.builder()
            .name("mock-embedding-v1")
            .provider("mock")
            .dimension(1536)
            .maxInputTokens(4096)
            .build();
}
