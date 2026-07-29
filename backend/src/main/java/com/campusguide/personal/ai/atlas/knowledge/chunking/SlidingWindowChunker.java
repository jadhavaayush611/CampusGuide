package com.campusguide.personal.ai.atlas.knowledge.chunking;

import com.campusguide.personal.ai.atlas.knowledge.artifact.*;
import com.campusguide.personal.ai.atlas.knowledge.ingestion.ArtifactBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Sliding window chunking strategy operating across word tokens with configurable window and overlap.
 */
@Component
public class SlidingWindowChunker implements ChunkingStrategy {

    private final ArtifactBuilder artifactBuilder;

    @Autowired
    public SlidingWindowChunker(ArtifactBuilder artifactBuilder) {
        this.artifactBuilder = artifactBuilder != null ? artifactBuilder : new ArtifactBuilder();
    }

    @Override
    public String getStrategyName() {
        return "SLIDING_WINDOW";
    }

    @Override
    public List<KnowledgeArtifact> chunk(KnowledgeArtifact documentArtifact, ChunkMetadata options) {
        if (documentArtifact == null || documentArtifact.getContent() == null || documentArtifact.getContent().isBlank()) {
            return List.of();
        }

        int windowWords = options != null && options.getChunkSize() > 0 ? options.getChunkSize() / 5 : 100; // ~5 chars/word
        if (windowWords <= 0) windowWords = 50;
        int overlapWords = options != null && options.getOverlap() >= 0 ? options.getOverlap() / 5 : 15;
        if (overlapWords >= windowWords) overlapWords = windowWords / 3;

        String content = documentArtifact.getContent();
        String[] words = content.split("\\s+");

        List<KnowledgeArtifact> chunks = new ArrayList<>();
        int step = windowWords - overlapWords;
        int index = 0;

        for (int start = 0; start < words.length; start += step) {
            int end = Math.min(start + windowWords, words.length);
            StringBuilder sb = new StringBuilder();
            for (int k = start; k < end; k++) {
                sb.append(words[k]).append(" ");
            }
            String chunkText = sb.toString().trim();

            if (!chunkText.isBlank()) {
                ArtifactSource chunkSource = ArtifactSource.builder()
                        .sourceUri(documentArtifact.getSource().getSourceUri())
                        .sourceType(documentArtifact.getSource().getSourceType())
                        .title(documentArtifact.getSource().getTitle())
                        .author(documentArtifact.getSource().getAuthor())
                        .startOffset(start)
                        .endOffset(end)
                        .build();

                KnowledgeArtifact chunkArtifact = artifactBuilder.buildChunkArtifact(
                        documentArtifact, chunkText, index++, chunkSource
                );
                chunkArtifact.getMetadata().put("strategy", getStrategyName());
                chunks.add(chunkArtifact);
            }

            if (end >= words.length) {
                break;
            }
        }

        linkSequentialChunks(chunks);
        return chunks;
    }

    private void linkSequentialChunks(List<KnowledgeArtifact> chunks) {
        for (int i = 0; i < chunks.size(); i++) {
            KnowledgeArtifact current = chunks.get(i);
            if (i > 0) {
                current.addReference(ArtifactReference.builder()
                        .targetArtifactId(chunks.get(i - 1).getId().getValue())
                        .referenceType(ArtifactReference.ReferenceType.PREVIOUS_CHUNK)
                        .build());
            }
            if (i < chunks.size() - 1) {
                current.addReference(ArtifactReference.builder()
                        .targetArtifactId(chunks.get(i + 1).getId().getValue())
                        .referenceType(ArtifactReference.ReferenceType.NEXT_CHUNK)
                        .build());
            }
        }
    }
}
