package com.campusguide.personal.ai.atlas.knowledge.embedding;

import com.campusguide.personal.ai.atlas.knowledge.artifact.ArtifactEmbedding;
import com.campusguide.personal.ai.atlas.knowledge.artifact.ArtifactVersion;
import com.campusguide.personal.ai.atlas.metrics.AtlasMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service managing embedding generation, batching, caching, retries, and provider dispatches.
 */
@Service
@Slf4j
public class EmbeddingService {

    private final Map<String, EmbeddingProvider> providers = new HashMap<>();
    private final Map<String, ArtifactEmbedding> cache = new ConcurrentHashMap<>();
    private final AtlasMetrics atlasMetrics;
    private final EmbeddingProvider defaultProvider;
    private int batchSize = 32;

    @Autowired
    public EmbeddingService(List<EmbeddingProvider> providerList, AtlasMetrics atlasMetrics) {
        this.atlasMetrics = atlasMetrics;
        if (providerList != null) {
            for (EmbeddingProvider p : providerList) {
                providers.put(p.getProviderName().toLowerCase(), p);
            }
        }
        this.defaultProvider = providers.getOrDefault("mock",
                providers.values().stream().findFirst().orElse(new MockEmbeddingProvider()));
    }

    public ArtifactEmbedding generateEmbedding(String text) {
        return generateEmbedding(text, "mock");
    }

    public ArtifactEmbedding generateEmbedding(String text, String providerName) {
        if (text == null || text.isBlank()) {
            return null;
        }

        String cacheKey = (providerName != null ? providerName : "default") + ":" + ArtifactVersion.computeChecksum(text);
        if (cache.containsKey(cacheKey)) {
            return cache.get(cacheKey);
        }

        EmbeddingRequest req = EmbeddingRequest.of(text);
        List<ArtifactEmbedding> result = generateEmbeddingsBatch(req, providerName);
        if (result != null && !result.isEmpty()) {
            ArtifactEmbedding emb = result.get(0);
            cache.put(cacheKey, emb);
            return emb;
        }
        return null;
    }

    public List<ArtifactEmbedding> generateEmbeddingsBatch(EmbeddingRequest request, String providerName) {
        if (request == null || request.getTexts() == null || request.getTexts().isEmpty()) {
            return List.of();
        }

        long startTime = System.currentTimeMillis();
        String pName = providerName != null ? providerName.toLowerCase() : "mock";
        EmbeddingProvider provider = providers.getOrDefault(pName, defaultProvider);

        List<String> texts = request.getTexts();
        List<ArtifactEmbedding> results = new ArrayList<>(Collections.nCopies(texts.size(), null));
        List<Integer> missingIndices = new ArrayList<>();
        List<String> missingTexts = new ArrayList<>();

        for (int i = 0; i < texts.size(); i++) {
            String text = texts.get(i);
            String cacheKey = provider.getProviderName() + ":" + ArtifactVersion.computeChecksum(text);
            if (cache.containsKey(cacheKey)) {
                results.set(i, cache.get(cacheKey));
            } else {
                missingIndices.add(i);
                missingTexts.add(text);
            }
        }

        if (!missingTexts.isEmpty()) {
            List<List<String>> batches = partition(missingTexts, batchSize);
            int batchOffset = 0;

            for (List<String> batch : batches) {
                EmbeddingRequest batchReq = EmbeddingRequest.of(batch, request.getModel());
                EmbeddingResponse resp = executeWithRetry(provider, batchReq);

                for (int j = 0; j < resp.getEmbeddings().size(); j++) {
                    ArtifactEmbedding emb = resp.getEmbeddings().get(j);
                    int origIndex = missingIndices.get(batchOffset + j);
                    results.set(origIndex, emb);

                    String cacheKey = provider.getProviderName() + ":" + ArtifactVersion.computeChecksum(batch.get(j));
                    cache.put(cacheKey, emb);
                }
                batchOffset += batch.size();
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        if (atlasMetrics != null) {
            atlasMetrics.recordProviderLatency(duration, provider.getProviderName());
        }

        return results;
    }

    private EmbeddingResponse executeWithRetry(EmbeddingProvider provider, EmbeddingRequest req) {
        int attempts = 0;
        int maxAttempts = 3;
        Exception lastException = null;

        while (attempts < maxAttempts) {
            attempts++;
            try {
                return provider.embed(req);
            } catch (Exception e) {
                lastException = e;
                log.warn("Attempt {} failed for embedding provider {}: {}", attempts, provider.getProviderName(), e.getMessage());
                try {
                    Thread.sleep(100L * attempts);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        log.error("All {} attempts failed for embedding provider {}", maxAttempts, provider.getProviderName());
        // Fallback to mock on repeated errors
        return new MockEmbeddingProvider(provider.getDimension()).embed(req);
    }

    private <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            partitions.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return partitions;
    }

    public void clearCache() {
        cache.clear();
    }

    public int getCacheSize() {
        return cache.size();
    }

    public void setBatchSize(int batchSize) {
        if (batchSize > 0) {
            this.batchSize = batchSize;
        }
    }
}
