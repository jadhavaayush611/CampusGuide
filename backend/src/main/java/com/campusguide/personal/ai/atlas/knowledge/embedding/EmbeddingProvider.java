package com.campusguide.personal.ai.atlas.knowledge.embedding;

/**
 * Provider-independent interface for vector embedding generation.
 */
public interface EmbeddingProvider {

    /**
     * Unique identifier for the provider (e.g. "openai", "mock", "local").
     */
    String getProviderName();

    /**
     * Default dimension produced by this provider.
     */
    int getDimension();

    /**
     * Generate vector embeddings for texts in the request.
     */
    EmbeddingResponse embed(EmbeddingRequest request);
}
