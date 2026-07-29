package com.campusguide.personal.ai.atlas.knowledge.artifact;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KnowledgeArtifactTest {

    @Test
    @DisplayName("Should initialize KnowledgeArtifact with generated ID and default lifecycle")
    void testArtifactInitialization() {
        ArtifactIdentifier id = ArtifactIdentifier.generate();
        KnowledgeArtifact artifact = KnowledgeArtifact.builder()
                .id(id)
                .type(ArtifactType.DOCUMENT)
                .content("Campus Guide RAG Architecture")
                .build();

        assertThat(artifact.getId()).isEqualTo(id);
        assertThat(artifact.getType()).isEqualTo(ArtifactType.DOCUMENT);
        assertThat(artifact.getContent()).isEqualTo("Campus Guide RAG Architecture");
        assertThat(artifact.getLifecycleState()).isEqualTo(ArtifactLifecycleState.DISCOVERED);
        assertThat(artifact.getVersion().getChecksum()).isNotBlank();
    }

    @Test
    @DisplayName("Should detect artifact type from extension or mime")
    void testArtifactTypeResolution() {
        assertThat(ArtifactType.fromExtensionOrMime("document.pdf")).isEqualTo(ArtifactType.PDF);
        assertThat(ArtifactType.fromExtensionOrMime("file.docx")).isEqualTo(ArtifactType.DOCX);
        assertThat(ArtifactType.fromExtensionOrMime("README.md")).isEqualTo(ArtifactType.MARKDOWN);
        assertThat(ArtifactType.fromExtensionOrMime("data.csv")).isEqualTo(ArtifactType.CSV);
        assertThat(ArtifactType.fromExtensionOrMime("https://example.org")).isEqualTo(ArtifactType.URL);
        assertThat(ArtifactType.fromExtensionOrMime("notes.txt")).isEqualTo(ArtifactType.TXT);
    }

    @Test
    @DisplayName("Should manage artifact references and retrieval hints")
    void testReferencesAndHints() {
        KnowledgeArtifact artifact = KnowledgeArtifact.builder()
                .id(ArtifactIdentifier.generate())
                .content("Sub-section content")
                .build();

        artifact.addReference(ArtifactReference.parent("art_parent123"));
        artifact.addRetrievalHint("searchWeight", 1.5);

        assertThat(artifact.getReferences()).hasSize(1);
        assertThat(artifact.getReferences().get(0).getTargetArtifactId()).isEqualTo("art_parent123");
        assertThat(artifact.getRetrievalHints()).containsEntry("searchWeight", 1.5);
    }

    @Test
    @DisplayName("Should update content and recalculate checksum version")
    void testVersionUpdate() {
        KnowledgeArtifact artifact = KnowledgeArtifact.builder()
                .id(ArtifactIdentifier.generate())
                .content("Version 1 content")
                .build();

        String originalChecksum = artifact.getVersion().getChecksum();
        artifact.updateContent("Version 2 updated content");

        assertThat(artifact.getContent()).isEqualTo("Version 2 updated content");
        assertThat(artifact.getVersion().getChecksum()).isNotEqualTo(originalChecksum);
    }
}
