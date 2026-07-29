package com.campusguide.personal.ai.atlas.knowledge.citation;

import com.campusguide.personal.ai.atlas.knowledge.artifact.ArtifactSource;
import com.campusguide.personal.ai.atlas.knowledge.artifact.KnowledgeArtifact;
import com.campusguide.personal.ai.atlas.knowledge.ranking.ArtifactScore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Generator producing structured Citations from ranked KnowledgeArtifacts.
 */
@Component
@Slf4j
public class CitationGenerator {

    public List<Citation> generateCitations(List<ArtifactScore> rankedScores) {
        if (rankedScores == null || rankedScores.isEmpty()) {
            return List.of();
        }

        List<Citation> citations = new ArrayList<>();

        for (int i = 0; i < rankedScores.size(); i++) {
            ArtifactScore score = rankedScores.get(i);
            KnowledgeArtifact artifact = score.getArtifact();
            if (artifact == null) continue;

            int citationIndex = i + 1;
            String citationMark = "[" + citationIndex + "]";

            ArtifactSource source = artifact.getSource() != null ? artifact.getSource() : new ArtifactSource();

            SourceReference sourceRef = SourceReference.builder()
                    .sourceUri(source.getSourceUri())
                    .sourceType(source.getSourceType())
                    .title(source.getTitle())
                    .author(source.getAuthor())
                    .build();

            DocumentReference docRef = DocumentReference.builder()
                    .documentId(artifact.getId().getValue())
                    .documentTitle(source.getTitle() != null ? source.getTitle() : artifact.getId().getValue())
                    .collectionId(artifact.getCollectionId())
                    .category(artifact.getMetadata() != null ? artifact.getMetadata().getCategory() : "general")
                    .build();

            SectionReference secRef = SectionReference.builder()
                    .sectionTitle(extractSectionTitle(artifact))
                    .startOffset(source.getStartOffset() != null ? source.getStartOffset() : 0)
                    .endOffset(source.getEndOffset() != null ? source.getEndOffset() : 0)
                    .chunkIndex(extractChunkIndex(artifact.getId().getValue()))
                    .build();

            String snippet = extractSnippet(artifact.getContent(), 250);

            Citation citation = Citation.builder()
                    .citationId("cite_" + citationIndex + "_" + artifact.getId().getValue())
                    .artifactId(artifact.getId().getValue())
                    .citationMark(citationMark)
                    .sourceReference(sourceRef)
                    .documentReference(docRef)
                    .sectionReference(secRef)
                    .snippet(snippet)
                    .confidenceScore(score.getTotalScore())
                    .build();

            citations.add(citation);
        }

        log.debug("Generated {} structured citations", citations.size());

        return citations;
    }

    private String extractSectionTitle(KnowledgeArtifact artifact) {
        if (artifact == null || artifact.getMetadata() == null) return "General";
        Object section = artifact.getMetadata().getAttributes().get("heading");
        if (section != null) return section.toString();
        return "Section 1";
    }

    private Integer extractChunkIndex(String artifactId) {
        if (artifactId == null || !artifactId.contains("_chk_")) return null;
        try {
            String idxStr = artifactId.substring(artifactId.lastIndexOf("_chk_") + 5);
            return Integer.parseInt(idxStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String extractSnippet(String content, int maxLen) {
        if (content == null || content.isBlank()) return "";
        String trimmed = content.trim().replaceAll("\\s+", " ");
        if (trimmed.length() <= maxLen) return trimmed;
        return trimmed.substring(0, maxLen - 3) + "...";
    }
}
