package com.campusguide.personal.ai.atlas.knowledge.catalog;

import com.campusguide.personal.ai.atlas.knowledge.artifact.ArtifactLifecycleState;
import com.campusguide.personal.ai.atlas.knowledge.artifact.ArtifactVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KnowledgeCatalogTest {

    private KnowledgeCatalog catalog;

    @BeforeEach
    void setUp() {
        KnowledgeRegistry registry = new KnowledgeRegistry();
        catalog = new KnowledgeCatalog(registry);
    }

    @Test
    @DisplayName("Should register catalog entries and track lifecycle transitions")
    void testCatalogEntryRegistration() {
        KnowledgeCatalogEntry entry = KnowledgeCatalogEntry.builder()
                .documentId("doc_handbook_01")
                .sourceUri("file://handbook.pdf")
                .sourceType("application/pdf")
                .title("Student Handbook")
                .checksum("checksum_abc123")
                .status(ArtifactLifecycleState.DISCOVERED)
                .build();

        catalog.registerEntry(entry);

        assertThat(catalog.getEntry("doc_handbook_01")).isPresent();
        assertThat(catalog.findByChecksum("checksum_abc123")).isPresent();
        assertThat(catalog.findByUri("file://handbook.pdf")).isPresent();

        catalog.updateStatus("doc_handbook_01", ArtifactLifecycleState.INDEXED, "Completed RAG indexing");

        KnowledgeCatalogEntry updated = catalog.getEntry("doc_handbook_01").get();
        assertThat(updated.getStatus()).isEqualTo(ArtifactLifecycleState.INDEXED);
        assertThat(updated.getDiagnostics()).isNotEmpty();
    }

    @Test
    @DisplayName("Should generate aggregated catalog summary statistics")
    void testCatalogSummary() {
        KnowledgeCatalogEntry entry1 = KnowledgeCatalogEntry.builder()
                .documentId("doc_1")
                .status(ArtifactLifecycleState.INDEXED)
                .totalChunks(5)
                .totalTokens(1000)
                .build();

        KnowledgeCatalogEntry entry2 = KnowledgeCatalogEntry.builder()
                .documentId("doc_2")
                .status(ArtifactLifecycleState.FAILED)
                .totalChunks(0)
                .totalTokens(0)
                .build();

        catalog.registerEntry(entry1);
        catalog.registerEntry(entry2);

        KnowledgeCatalog.CatalogSummary summary = catalog.getSummary();

        assertThat(summary.totalDocuments()).isEqualTo(2);
        assertThat(summary.totalChunks()).isEqualTo(5);
        assertThat(summary.totalTokens()).isEqualTo(1000);
        assertThat(summary.countsByStatus().get(ArtifactLifecycleState.INDEXED)).isEqualTo(1L);
        assertThat(summary.countsByStatus().get(ArtifactLifecycleState.FAILED)).isEqualTo(1L);
    }
}
