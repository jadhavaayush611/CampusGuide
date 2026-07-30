package com.campusguide.personal.ai.atlas.knowledge.graph;

import com.campusguide.personal.ai.atlas.knowledge.artifact.*;
import com.campusguide.personal.ai.atlas.knowledge.collection.KnowledgeCollectionScope;
import com.campusguide.personal.ai.atlas.knowledge.collection.KnowledgeCollection;
import com.campusguide.personal.ai.atlas.knowledge.graph.edge.KnowledgeEdge;
import com.campusguide.personal.ai.atlas.knowledge.graph.edge.RelationshipStrength;
import com.campusguide.personal.ai.atlas.knowledge.graph.edge.RelationshipType;
import com.campusguide.personal.ai.atlas.knowledge.graph.extraction.*;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RelationshipExtractionTest {

    private RelationshipRegistry registry;

    @BeforeEach
    void setUp() {
        ArtifactReferenceRelationshipExtractor refExtractor = new ArtifactReferenceRelationshipExtractor();
        CollectionRelationshipExtractor colExtractor = new CollectionRelationshipExtractor();
        MetadataRelationshipExtractor metaExtractor = new MetadataRelationshipExtractor();
        registry = new RelationshipRegistry(List.of(refExtractor, colExtractor, metaExtractor));
    }

    @Test
    @DisplayName("Should build edge cleanly using RelationshipBuilder")
    void testRelationshipBuilder() {
        NodeIdentifier src = NodeIdentifier.of("src_1");
        NodeIdentifier tgt = NodeIdentifier.of("tgt_1");

        KnowledgeEdge edge = RelationshipBuilder.create()
                .source(src)
                .target(tgt)
                .relationship(RelationshipType.DEPENDS_ON)
                .strength(RelationshipStrength.STRONG)
                .extractor("test-extractor")
                .provenance("test-provenance")
                .build();

        assertThat(edge.getSourceNodeId()).isEqualTo(src);
        assertThat(edge.getTargetNodeId()).isEqualTo(tgt);
        assertThat(edge.getRelationshipType()).isEqualTo(RelationshipType.DEPENDS_ON);
        assertThat(edge.getStrength()).isEqualTo(RelationshipStrength.STRONG);
        assertThat(edge.getMetadata().getExtractorName()).isEqualTo("test-extractor");
    }

    @Test
    @DisplayName("Should extract explicit references from artifact")
    void testArtifactReferenceExtractor() {
        ArtifactIdentifier art1 = ArtifactIdentifier.of("art_1");
        ArtifactIdentifier art2 = ArtifactIdentifier.of("art_2");

        KnowledgeArtifact artifact = KnowledgeArtifact.builder()
                .id(art1)
                .collectionId("col_1")
                .references(List.of(
                        ArtifactReference.builder().targetArtifactId(art2.getValue()).referenceType(ArtifactReference.ReferenceType.CROSS_REFERENCE).build()
                ))
                .build();

        List<KnowledgeEdge> edges = registry.extractFromArtifact(artifact);
        assertThat(edges).isNotEmpty();

        boolean hasRef = edges.stream().anyMatch(e -> e.getRelationshipType() == RelationshipType.REFERENCES);
        assertThat(hasRef).isTrue();
    }

    @Test
    @DisplayName("Should extract CONTAINS and BELONGS_TO relationships for collection")
    void testCollectionExtractor() {
        KnowledgeCollection collection = KnowledgeCollection.builder()
                .collectionId("col_syllabus")
                .name("Syllabus Collection")
                .scope(KnowledgeCollectionScope.PUBLIC)
                .build();

        KnowledgeArtifact art1 = KnowledgeArtifact.builder()
                .id(ArtifactIdentifier.of("art_1"))
                .collectionId(collection.getCollectionId())
                .build();

        List<KnowledgeEdge> edges = registry.extractFromCollection(collection, List.of(art1));
        assertThat(edges).hasSize(2); // 1 CONTAINS + 1 BELONGS_TO from collection relationship extraction

        boolean hasContains = edges.stream().anyMatch(e -> e.getRelationshipType() == RelationshipType.CONTAINS);
        boolean hasBelongsTo = edges.stream().anyMatch(e -> e.getRelationshipType() == RelationshipType.BELONGS_TO);
        assertThat(hasContains).isTrue();
        assertThat(hasBelongsTo).isTrue();
    }

    @Test
    @DisplayName("Should extract domain entity relationships from artifact metadata")
    void testMetadataExtractor() {
        ArtifactMetadata metadata = new ArtifactMetadata();
        metadata.setAttributes(Map.of(
                "courseCode", "CS101",
                "instructor", "ProfTuring",
                "building", "TechHall",
                "department", "CompSci"
        ));

        KnowledgeArtifact artifact = KnowledgeArtifact.builder()
                .id(ArtifactIdentifier.of("syllabus_doc"))
                .metadata(metadata)
                .build();

        List<KnowledgeEdge> edges = registry.extractFromArtifact(artifact);
        assertThat(edges).isNotEmpty();

        boolean hasTeaches = edges.stream().anyMatch(e -> e.getRelationshipType() == RelationshipType.TEACHES);
        boolean hasLocatedIn = edges.stream().anyMatch(e -> e.getRelationshipType() == RelationshipType.LOCATED_IN);
        boolean hasPartOf = edges.stream().anyMatch(e -> e.getRelationshipType() == RelationshipType.PART_OF);

        assertThat(hasTeaches).isTrue();
        assertThat(hasLocatedIn).isTrue();
        assertThat(hasPartOf).isTrue();
    }
}
