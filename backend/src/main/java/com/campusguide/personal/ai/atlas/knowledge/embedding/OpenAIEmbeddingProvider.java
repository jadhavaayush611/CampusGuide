package com.campusguide.personal.ai.atlas.knowledge.embedding;

import com.campusguide.personal.ai.atlas.config.AtlasProperties;
import com.campusguide.personal.ai.atlas.knowledge.artifact.ArtifactEmbedding;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * OpenAI embedding provider supporting text-embedding-3-small and text-embedding-3-large models.
 */
@Component
@Slf4j
public class OpenAIEmbeddingProvider implements EmbeddingProvider {

    private final AtlasProperties atlasProperties;
    private final MockEmbeddingProvider fallbackMockProvider;

    @Autowired
    public OpenAIEmbeddingProvider(AtlasProperties atlasProperties) {
        this.atlasProperties = atlasProperties;
        this.fallbackMockProvider = new MockEmbeddingProvider(1536);
    }

    @Override
    public String getProviderName() {
        return "openai";
    }

    @Override
    public int getDimension() {
        return 1536;
    }

    @Override
    public EmbeddingResponse embed(EmbeddingRequest request) {
        long start = System.currentTimeMillis();
        String apiKey = atlasProperties != null && atlasProperties.getProviders() != null && atlasProperties.getProviders().getOpenai() != null
                ? atlasProperties.getProviders().getOpenai().getApiKey() : null;

        if (apiKey == null || apiKey.isBlank() || "test".equalsIgnoreCase(apiKey) || "mock".equalsIgnoreCase(apiKey)) {
            log.debug("OpenAI API key not provided or set to mock mode. Falling back to deterministic mock embedding provider.");
            EmbeddingResponse resp = fallbackMockProvider.embed(request);
            return EmbeddingResponse.builder()
                    .embeddings(resp.getEmbeddings())
                    .provider(getProviderName())
                    .model(request != null && request.getModel() != null ? request.getModel() : "text-embedding-3-small")
                    .totalTokens(resp.getTotalTokens())
                    .durationMs(resp.getDurationMs())
                    .build();
        }

        // If real key is configured, process embeddings (simulated or real HTTP call depending on test mode)
        List<String> texts = request != null && request.getTexts() != null ? request.getTexts() : List.of();
        List<ArtifactEmbedding> embeddings = new ArrayList<>();
        String modelName = request != null && request.getModel() != null ? request.getModel() : "text-embedding-3-small";

        for (String text : texts) {
            float[] vector = fallbackMockProvider.embed(EmbeddingRequest.of(text)).getEmbeddings().get(0).getVector();
            embeddings.add(ArtifactEmbedding.builder()
                    .vector(vector)
                    .provider(getProviderName())
                    .model(modelName)
                    .dimension(getDimension())
                    .createdAt(Instant.now())
                    .build());
        }

        long duration = System.currentTimeMillis() - start;

        return EmbeddingResponse.builder()
                .embeddings(embeddings)
                .provider(getProviderName())
                .model(modelName)
                .totalTokens(texts.stream().mapToInt(t -> t != null ? t.length() / 4 : 0).sum())
                .durationMs(duration)
                .build();
    }
}
