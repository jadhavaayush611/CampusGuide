package com.campusguide.personal.ai.atlas.knowledge.vector;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory vector store implementation with cosine similarity search and metadata filtering.
 */
@Component
public class InMemoryVectorStore implements VectorStore {

    private final Map<String, VectorRecord> storage = new ConcurrentHashMap<>();
    private final VectorIndex index;

    public InMemoryVectorStore() {
        this.index = VectorIndex.defaultIndex();
    }

    public InMemoryVectorStore(VectorIndex index) {
        this.index = index != null ? index : VectorIndex.defaultIndex();
    }

    @Override
    public void index(VectorRecord record) {
        if (record == null || record.getArtifactId() == null) {
            return;
        }
        storage.put(record.getArtifactId(), record);
    }

    @Override
    public void indexAll(List<VectorRecord> records) {
        if (records == null) return;
        for (VectorRecord r : records) {
            index(r);
        }
    }

    @Override
    public List<VectorRecord> search(float[] queryVector, int topK, VectorMetadata filter) {
        if (queryVector == null || storage.isEmpty() || topK <= 0) {
            return List.of();
        }

        List<ScoredRecord> scored = new ArrayList<>();

        for (VectorRecord record : storage.values()) {
            if (record.getVector() == null) continue;

            if (filter != null && !record.getMetadata().matchesFilter(filter)) {
                continue;
            }

            double similarity = cosineSimilarity(queryVector, record.getVector());

            VectorMetadata updatedMeta = VectorMetadata.builder()
                    .documentId(record.getMetadata().getDocumentId())
                    .sourceType(record.getMetadata().getSourceType())
                    .category(record.getMetadata().getCategory())
                    .domain(record.getMetadata().getDomain())
                    .score(similarity)
                    .distance(1.0 - similarity)
                    .fields(new HashMap<>(record.getMetadata().getFields()))
                    .build();

            VectorRecord resultRecord = VectorRecord.builder()
                    .artifactId(record.getArtifactId())
                    .vector(record.getVector())
                    .metadata(updatedMeta)
                    .artifact(record.getArtifact())
                    .build();

            scored.add(new ScoredRecord(resultRecord, similarity));
        }

        scored.sort((a, b) -> Double.compare(b.similarity, a.similarity));

        List<VectorRecord> results = new ArrayList<>();
        for (int i = 0; i < Math.min(topK, scored.size()); i++) {
            results.add(scored.get(i).record);
        }

        return results;
    }

    @Override
    public Optional<VectorRecord> get(String artifactId) {
        if (artifactId == null) return Optional.empty();
        return Optional.ofNullable(storage.get(artifactId));
    }

    @Override
    public boolean delete(String artifactId) {
        if (artifactId == null) return false;
        return storage.remove(artifactId) != null;
    }

    @Override
    public void clear() {
        storage.clear();
    }

    @Override
    public int count() {
        return storage.size();
    }

    @Override
    public String getProviderName() {
        return "in-memory";
    }

    @Override
    public VectorIndex getIndex() {
        return index;
    }

    private double cosineSimilarity(float[] v1, float[] v2) {
        if (v1 == null || v2 == null || v1.length == 0 || v2.length == 0) return 0.0;
        int len = Math.min(v1.length, v2.length);
        double dot = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < len; i++) {
            dot += v1[i] * v2[i];
            norm1 += v1[i] * v1[i];
            norm2 += v2[i] * v2[i];
        }

        if (norm1 <= 0.0 || norm2 <= 0.0) return 0.0;
        return dot / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    private record ScoredRecord(VectorRecord record, double similarity) {}
}
