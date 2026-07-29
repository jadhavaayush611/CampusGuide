package com.campusguide.personal.ai.atlas.knowledge.chunking;

import com.campusguide.personal.ai.atlas.knowledge.artifact.*;
import com.campusguide.personal.ai.atlas.knowledge.ingestion.ArtifactBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Fixed-size chunking strategy with configurable size and overlap.
 */
@Component
public class FixedSizeChunker implements ChunkingStrategy {

    private final ArtifactBuilder artifactBuilder;

    @Autowired
    public FixedSizeChunker(ArtifactBuilder artifactBuilder) {
        this.artifactBuilder = artifactBuilder != null ? artifactBuilder : new ArtifactBuilder();
    }

    @Override
    public String getStrategyName() {
        return "FIXED_SIZE";
    }

    @Override
    public List<KnowledgeArtifact> chunk(KnowledgeArtifact documentArtifact, ChunkMetadata options) {
        if (documentArtifact == null || documentArtifact.getContent() == null || documentArtifact.getContent().isBlank()) {
            return List.of();
        }

        int chunkSize = options != null && options.getChunkSize() > 0 ? options.getChunkSize() : 512;
        int overlap = options != null && options.getOverlap() >= 0 ? options.getOverlap() : 64;
        if (overlap >= chunkSize) {
            overlap = Math.max(0, chunkSize / 4);
        }

        String content = documentArtifact.getContent();
        List<KnowledgeArtifact> chunks = new ArrayList<>();
        int step = chunkSize - overlap;
        int index = 0;

        for (int start = 0; start < content.length(); start += step) {
            int end = Math.min(start + chunkSize, content.length());
            String chunkText = content.substring(start, end).trim();

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

            if (end >= content.length()) {
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
