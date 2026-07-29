package com.campusguide.personal.ai.atlas.knowledge.citation;

import com.campusguide.personal.ai.atlas.knowledge.artifact.ArtifactIdentifier;
import com.campusguide.personal.ai.atlas.knowledge.artifact.ArtifactMetadata;
import com.campusguide.personal.ai.atlas.knowledge.artifact.ArtifactSource;
import com.campusguide.personal.ai.atlas.knowledge.artifact.KnowledgeArtifact;
import com.campusguide.personal.ai.atlas.knowledge.ranking.ArtifactScore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CitationGeneratorTest {

    @Test
    @DisplayName("Should generate structured citations with citation marks and reference breakdowns")
    void testCitationGeneration() {
        CitationGenerator generator = new CitationGenerator();

        KnowledgeArtifact artifact = KnowledgeArtifact.builder()
                .id(ArtifactIdentifier.of("doc_101_chk_2"))
                .content("The Mathematics building is located north of the Student Center.")
                .collectionId("public_campus_knowledge")
                .metadata(ArtifactMetadata.builder()
                        .category("campus_info")
                        .attributes(Map.of("heading", "Building Locations"))
                        .build())
                .source(ArtifactSource.builder()
                        .title("Campus Navigation Guide")
                        .sourceUri("https://campus.edu/map")
                        .sourceType("pdf")
                        .author("Facilities Dept")
                        .startOffset(100)
                        .endOffset(250)
                        .build())
                .build();

        ArtifactScore score = ArtifactScore.builder()
                .artifact(artifact)
                .totalScore(0.88)
                .build();

        List<Citation> citations = generator.generateCitations(List.of(score));

        assertEquals(1, citations.size());
        Citation citation = citations.get(0);

        assertEquals("[1]", citation.getCitationMark());
        assertEquals("doc_101_chk_2", citation.getArtifactId());
        assertEquals("Campus Navigation Guide", citation.getSourceReference().getTitle());
        assertEquals("https://campus.edu/map", citation.getSourceReference().getSourceUri());
        assertEquals("public_campus_knowledge", citation.getDocumentReference().getCollectionId());
        assertEquals("Building Locations", citation.getSectionReference().getSectionTitle());
        assertEquals(2, citation.getSectionReference().getChunkIndex());
        assertTrue(citation.getSnippet().contains("Mathematics building"));
    }
}
