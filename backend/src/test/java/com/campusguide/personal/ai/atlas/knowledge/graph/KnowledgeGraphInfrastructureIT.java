package com.campusguide.personal.ai.atlas.knowledge.graph;

import com.campusguide.personal.ai.atlas.knowledge.artifact.ArtifactIdentifier;
import com.campusguide.personal.ai.atlas.knowledge.artifact.ArtifactMetadata;
import com.campusguide.personal.ai.atlas.knowledge.artifact.ArtifactReference;
import com.campusguide.personal.ai.atlas.knowledge.artifact.KnowledgeArtifact;
import com.campusguide.personal.ai.atlas.knowledge.collection.KnowledgeCollectionScope;
import com.campusguide.personal.ai.atlas.knowledge.collection.KnowledgeCollection;
import com.campusguide.personal.ai.atlas.knowledge.graph.construction.GraphConstructionService;
import com.campusguide.personal.ai.atlas.knowledge.graph.extraction.ArtifactReferenceRelationshipExtractor;
import com.campusguide.personal.ai.atlas.knowledge.graph.extraction.CollectionRelationshipExtractor;
import com.campusguide.personal.ai.atlas.knowledge.graph.extraction.MetadataRelationshipExtractor;
import com.campusguide.personal.ai.atlas.knowledge.graph.extraction.RelationshipRegistry;
import com.campusguide.personal.ai.atlas.knowledge.graph.lifecycle.GraphLifecycleState;
import com.campusguide.personal.ai.atlas.knowledge.graph.model.KnowledgeGraph;
import com.campusguide.personal.ai.atlas.knowledge.graph.model.KnowledgePath;
import com.campusguide.personal.ai.atlas.knowledge.graph.model.KnowledgeSubgraph;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeIdentifier;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeType;
import com.campusguide.personal.ai.atlas.knowledge.graph.registry.GraphCatalogEntry;
import com.campusguide.personal.ai.atlas.knowledge.graph.registry.KnowledgeGraphRegistry;
import com.campusguide.personal.ai.atlas.knowledge.graph.storage.GraphRepository;
import com.campusguide.personal.ai.atlas.knowledge.graph.storage.InMemoryGraphStore;
import com.campusguide.personal.ai.atlas.knowledge.graph.traversal.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class KnowledgeGraphInfrastructureIT {

    private GraphConstructionService constructionService;
    private GraphRepository repository;
    private KnowledgeGraphRegistry registry;
    private DefaultGraphTraversalEngine traversalEngine;

    @BeforeEach
    void setUp() {
        RelationshipRegistry relationshipRegistry = new RelationshipRegistry(List.of(
                new ArtifactReferenceRelationshipExtractor(),
                new CollectionRelationshipExtractor(),
                new MetadataRelationshipExtractor()
        ));

        constructionService = new GraphConstructionService(relationshipRegistry, null);
        InMemoryGraphStore store = new InMemoryGraphStore();
        repository = new GraphRepository(store);
        registry = new KnowledgeGraphRegistry(repository);

        BreadthFirstTraversal bfs = new BreadthFirstTraversal();
        DepthFirstTraversal dfs = new DepthFirstTraversal();
        NeighborhoodTraversal neighborhood = new NeighborhoodTraversal(bfs);
        traversalEngine = new DefaultGraphTraversalEngine(bfs, dfs, neighborhood, null);
    }

    @Test
    @DisplayName("Complete Integration Test: Construct, store, register, and traverse Knowledge Graph")
    void testEndToEndKnowledgeGraphPipeline() {
        // 1. Prepare Collection and KnowledgeArtifacts
        KnowledgeCollection collection = KnowledgeCollection.builder()
                .collectionId("col_cs101")
                .name("Computer Science 101 Course Pack")
                .scope(com.campusguide.personal.ai.atlas.knowledge.collection.KnowledgeCollectionScope.PUBLIC)
                .build();

        ArtifactMetadata doc1Meta = new ArtifactMetadata();
        doc1Meta.setName("Course Syllabus");
        doc1Meta.setAttributes(Map.of(
                "courseCode", "CS101",
                "instructor", "ProfTuring",
                "building", "TechHall"
        ));

        ArtifactIdentifier art1Id = ArtifactIdentifier.of("doc_syllabus");
        ArtifactIdentifier art2Id = ArtifactIdentifier.of("doc_lecture1");

        KnowledgeArtifact doc1 = KnowledgeArtifact.builder()
                .id(art1Id)
                .collectionId(collection.getCollectionId())
                .metadata(doc1Meta)
                .references(List.of(
                        ArtifactReference.builder().targetArtifactId(art2Id.getValue()).referenceType(ArtifactReference.ReferenceType.NEXT_CHUNK).build()
                ))
                .build();

        ArtifactMetadata doc2Meta = new ArtifactMetadata();
        doc2Meta.setName("Lecture 1 - Algorithms");
        doc2Meta.setAttributes(Map.of("courseCode", "CS101"));

        KnowledgeArtifact doc2 = KnowledgeArtifact.builder()
                .id(art2Id)
                .collectionId(collection.getCollectionId())
                .metadata(doc2Meta)
                .build();

        // 2. Construct KnowledgeGraph
        KnowledgeGraph graph = constructionService.buildGraphFromCollection(
                "cs101_full_graph",
                collection,
                List.of(doc1, doc2)
        );

        assertThat(graph).isNotNull();
        assertThat(graph.getMetadata().getNodeCount()).isGreaterThan(3);
        assertThat(graph.getMetadata().getEdgeCount()).isGreaterThan(3);

        // 3. Store and Register Graph
        registry.registerGraph(graph);

        Optional<GraphCatalogEntry> catalogOpt = registry.getCatalogEntry("cs101_full_graph");
        assertThat(catalogOpt).isPresent();
        assertThat(catalogOpt.get().getLifecycleState()).isEqualTo(GraphLifecycleState.ACTIVE);

        // 4. Perform Deterministic Traversals
        NodeIdentifier startNode = NodeIdentifier.ofArtifact(art1Id);
        TraversalPolicy policy = TraversalPolicy.builder()
                .maxDepth(3)
                .direction(TraversalPolicy.Direction.OUTGOING)
                .build();

        List<KnowledgePath> paths = traversalEngine.traverse(graph, startNode, policy);
        assertThat(paths).isNotEmpty();

        // Extract neighborhood
        KnowledgeSubgraph neighborhood = traversalEngine.extractNeighborhood(graph, startNode, policy);
        assertThat(neighborhood).isNotNull();
        assertThat(neighborhood.containsNode(startNode)).isTrue();
        assertThat(neighborhood.getNodeCount()).isGreaterThan(1);
    }
}
