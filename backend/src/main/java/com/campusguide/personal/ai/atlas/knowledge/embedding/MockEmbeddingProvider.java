package com.campusguide.personal.ai.atlas.knowledge.embedding;

import com.campusguide.personal.ai.atlas.knowledge.artifact.ArtifactEmbedding;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Deterministic mock embedding provider for testing and offline execution.
 */
@Component
public class MockEmbeddingProvider implements EmbeddingProvider {

    private final int dimension;

    public MockEmbeddingProvider() {
        this.dimension = 1536;
    }

    public MockEmbeddingProvider(int dimension) {
        this.dimension = dimension;
    }

    @Override
    public String getProviderName() {
        return "mock";
    }

    @Override
    public int getDimension() {
        return dimension;
    }

    @Override
    public EmbeddingResponse embed(EmbeddingRequest request) {
        long start = System.currentTimeMillis();
        List<ArtifactEmbedding> embeddings = new ArrayList<>();
        int totalTokens = 0;

        String model = request != null && request.getModel() != null ? request.getModel() : "mock-embedding-v1";
        List<String> texts = request != null && request.getTexts() != null ? request.getTexts() : List.of();

        for (String text : texts) {
            float[] vector = generateDeterministicVector(text, dimension);
            embeddings.add(ArtifactEmbedding.builder()
                    .vector(vector)
                    .provider(getProviderName())
                    .model(model)
                    .dimension(dimension)
                    .createdAt(Instant.now())
                    .build());
            totalTokens += (text != null ? text.length() / 4 : 0);
        }

        long duration = System.currentTimeMillis() - start;

        return EmbeddingResponse.builder()
                .embeddings(embeddings)
                .provider(getProviderName())
                .model(model)
                .totalTokens(totalTokens)
                .durationMs(duration)
                .build();
    }

    private float[] generateDeterministicVector(String text, int dim) {
        float[] vector = new float[dim];
        if (text == null) text = "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            float norm = 0.0f;
            for (int i = 0; i < dim; i++) {
                byte b = hash[i % hash.length];
                float val = ((b & 0xFF) - 128) / 128.0f + (float) Math.sin((i + 1) * text.hashCode());
                vector[i] = val;
                norm += val * val;
            }
            norm = (float) Math.sqrt(norm);
            if (norm > 0.00001f) {
                for (int i = 0; i < dim; i++) {
                    vector[i] /= norm;
                }
            }
        } catch (Exception e) {
            for (int i = 0; i < dim; i++) {
                vector[i] = 1.0f / (float) Math.sqrt(dim);
            }
        }
        return vector;
    }
}
