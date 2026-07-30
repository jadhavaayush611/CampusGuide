package com.campusguide.personal.ai.atlas.knowledge.graph;

import com.campusguide.personal.ai.atlas.knowledge.artifact.ArtifactIdentifier;
import com.campusguide.personal.ai.atlas.knowledge.artifact.ArtifactMetadata;
import com.campusguide.personal.ai.atlas.knowledge.artifact.KnowledgeArtifact;
import com.campusguide.personal.ai.atlas.knowledge.collection.KnowledgeCollection;
import com.campusguide.personal.ai.atlas.knowledge.graph.construction.GraphConstructionService;
import com.campusguide.personal.ai.atlas.knowledge.graph.extraction.ArtifactReferenceRelationshipExtractor;
import com.campusguide.personal.ai.atlas.knowledge.graph.extraction.CollectionRelationshipExtractor;
import com.campusguide.personal.ai.atlas.knowledge.graph.extraction.MetadataRelationshipExtractor;
import com.campusguide.personal.ai.atlas.knowledge.graph.extraction.RelationshipRegistry;
import com.campusguide.personal.ai.atlas.knowledge.graph.lifecycle.GraphLifecycleState;
import com.campusguide.personal.ai.atlas.knowledge.graph.model.KnowledgeGraph;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GraphConstructionServiceTest {

    private GraphConstructionService constructionService;

    @BeforeEach
    void setUp() {
        RelationshipRegistry registry = new RelationshipRegistry(List.of(
                new ArtifactReferenceRelationshipExtractor(),
                new CollectionRelationshipExtractor(),
                new MetadataRelationshipExtractor()
        ));
        constructionService = new GraphConstructionService(registry, null);
    }

    @Test
    @DisplayName("Should build graph from artifacts list with synthesized entity nodes")
    void testBuildGraphFromArtifacts() {
        ArtifactMetadata meta1 = new ArtifactMetadata();
        meta1.setName("CS101 Syllabus");
        meta1.setAttributes(Map.of("courseCode", "CS101", "instructor", "Dr. Alan"));

        KnowledgeArtifact artifact1 = KnowledgeArtifact.builder()
                .id(ArtifactIdentifier.of("art_cs101"))
                .collectionId("col_cs")
                .metadata(meta1)
                .build();

        KnowledgeGraph graph = constructionService.buildGraphFromArtifacts("cs_graph", List.of(artifact1));

        assertThat(graph).isNotNull();
        assertThat(graph.getMetadata().getGraphId()).isEqualTo("cs_graph");
        assertThat(graph.getMetadata().getLifecycleState()).isEqualTo(GraphLifecycleState.ACTIVE);
        assertThat(graph.getNodes()).isNotEmpty();

        // Artifact node, Course node, Person node
        assertThat(graph.getNodesByType(NodeType.KNOWLEDGE_ARTIFACT)).hasSize(1);
        assertThat(graph.getNodesByType(NodeType.COURSE)).hasSize(1);
        assertThat(graph.getNodesByType(NodeType.PERSON)).hasSize(1);

        assertThat(graph.getEdges()).isNotEmpty();
    }

    @Test
    @DisplayName("Should build graph from collection with collection node and membership edges")
    void testBuildGraphFromCollection() {
        KnowledgeCollection collection = KnowledgeCollection.builder()
                .collectionId("col_eng")
                .name("Engineering Syllabi")
                .build();

        KnowledgeArtifact art1 = KnowledgeArtifact.builder()
                .id(ArtifactIdentifier.of("art_eng1"))
                .collectionId(collection.getCollectionId())
                .build();

        KnowledgeGraph graph = constructionService.buildGraphFromCollection("eng_graph", collection, List.of(art1));

        assertThat(graph).isNotNull();
        assertThat(graph.getNodesByType(NodeType.KNOWLEDGE_COLLECTION)).hasSize(1);
        assertThat(graph.getNodesByType(NodeType.KNOWLEDGE_ARTIFACT)).hasSize(1);
        assertThat(graph.getEdges()).isNotEmpty();
    }
}
