package com.campusguide.personal.ai.atlas.knowledge.chunking;

import com.campusguide.personal.ai.atlas.knowledge.artifact.*;
import com.campusguide.personal.ai.atlas.knowledge.ingestion.ArtifactBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Semantic chunking strategy that respects section boundaries, paragraphs, and sentence structures.
 */
@Component
public class SemanticChunker implements ChunkingStrategy {

    private static final Pattern PARAGRAPH_SPLIT = Pattern.compile("\n{2,}");
    private static final Pattern HEADING_PATTERN = Pattern.compile("^(?:#{1,6}\\s+.*|[A-Z0-9\\s\\-]{3,60}:?)$");

    private final ArtifactBuilder artifactBuilder;

    @Autowired
    public SemanticChunker(ArtifactBuilder artifactBuilder) {
        this.artifactBuilder = artifactBuilder != null ? artifactBuilder : new ArtifactBuilder();
    }

    @Override
    public String getStrategyName() {
        return "SEMANTIC";
    }

    @Override
    public List<KnowledgeArtifact> chunk(KnowledgeArtifact documentArtifact, ChunkMetadata options) {
        if (documentArtifact == null || documentArtifact.getContent() == null || documentArtifact.getContent().isBlank()) {
            return List.of();
        }

        int targetChunkSize = options != null && options.getChunkSize() > 0 ? options.getChunkSize() : 512;

        String content = documentArtifact.getContent();
        String[] paragraphs = PARAGRAPH_SPLIT.split(content);

        List<KnowledgeArtifact> chunks = new ArrayList<>();
        StringBuilder currentBuffer = new StringBuilder();
        String currentSectionHeading = "Overview";
        int index = 0;

        for (String paragraph : paragraphs) {
            String trimmedPara = paragraph.trim();
            if (trimmedPara.isEmpty()) continue;

            if (HEADING_PATTERN.matcher(trimmedPara).matches()) {
                if (currentBuffer.length() > 0) {
                    chunks.add(createSemanticChunk(documentArtifact, currentBuffer.toString().trim(), index++, currentSectionHeading));
                    currentBuffer.setLength(0);
                }
                currentSectionHeading = trimmedPara.replaceAll("^#{1,6}\\s+", "");
            }

            if (currentBuffer.length() + trimmedPara.length() + 2 > targetChunkSize && currentBuffer.length() > 0) {
                chunks.add(createSemanticChunk(documentArtifact, currentBuffer.toString().trim(), index++, currentSectionHeading));
                currentBuffer.setLength(0);
            }

            if (currentBuffer.length() > 0) {
                currentBuffer.append("\n\n");
            }
            currentBuffer.append(trimmedPara);
        }

        if (currentBuffer.length() > 0) {
            chunks.add(createSemanticChunk(documentArtifact, currentBuffer.toString().trim(), index++, currentSectionHeading));
        }

        linkSequentialChunks(chunks);
        return chunks;
    }

    private KnowledgeArtifact createSemanticChunk(KnowledgeArtifact parent, String content, int index, String sectionHeading) {
        ArtifactSource source = ArtifactSource.builder()
                .sourceUri(parent.getSource().getSourceUri())
                .sourceType(parent.getSource().getSourceType())
                .title(parent.getSource().getTitle())
                .author(parent.getSource().getAuthor())
                .build();

        KnowledgeArtifact chunk = artifactBuilder.buildChunkArtifact(parent, content, index, source);
        chunk.getMetadata().put("strategy", getStrategyName());
        chunk.getMetadata().put("sectionHeading", sectionHeading);
        return chunk;
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
