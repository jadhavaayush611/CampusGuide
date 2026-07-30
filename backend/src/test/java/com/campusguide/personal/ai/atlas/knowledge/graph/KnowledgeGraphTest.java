package com.campusguide.personal.ai.atlas.knowledge.graph;

import com.campusguide.personal.ai.atlas.knowledge.graph.edge.KnowledgeEdge;
import com.campusguide.personal.ai.atlas.knowledge.graph.edge.RelationshipStrength;
import com.campusguide.personal.ai.atlas.knowledge.graph.edge.RelationshipType;
import com.campusguide.personal.ai.atlas.knowledge.graph.model.KnowledgeGraph;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeAttributes;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeIdentifier;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.KnowledgeNode;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KnowledgeGraphTest {

    private KnowledgeGraph graph;

    @BeforeEach
    void setUp() {
        graph = KnowledgeGraph.create("test_graph", "Test Knowledge Graph");
    }

    @Test
    @DisplayName("Should create graph and verify initial metadata state")
    void testGraphCreation() {
        assertThat(graph.getMetadata().getGraphId()).isEqualTo("test_graph");
        assertThat(graph.getMetadata().getName()).isEqualTo("Test Knowledge Graph");
        assertThat(graph.getNodes()).isEmpty();
        assertThat(graph.getEdges()).isEmpty();
    }

    @Test
    @DisplayName("Should add, retrieve, and remove nodes cleanly")
    void testNodeManagement() {
        NodeIdentifier nodeId = NodeIdentifier.of(NodeType.COURSE, "CS101");
        KnowledgeNode node = KnowledgeNode.builder()
                .id(nodeId)
                .type(NodeType.COURSE)
                .name("Intro to CS")
                .attributes(NodeAttributes.of(Map.of("credits", 4)))
                .build();

        graph.addNode(node);

        assertThat(graph.containsNode(nodeId)).isTrue();
        assertThat(graph.getNode(nodeId)).isNotNull();
        assertThat(graph.getNode(nodeId).getName()).isEqualTo("Intro to CS");
        assertThat(graph.getNodesByType(NodeType.COURSE)).hasSize(1);

        KnowledgeNode removed = graph.removeNode(nodeId);
        assertThat(removed).isNotNull();
        assertThat(graph.containsNode(nodeId)).isFalse();
    }

    @Test
    @DisplayName("Should add, retrieve, and remove edges cleanly")
    void testEdgeManagement() {
        NodeIdentifier nodeA = NodeIdentifier.of("node_a");
        NodeIdentifier nodeB = NodeIdentifier.of("node_b");

        graph.addNode(KnowledgeNode.builder().id(nodeA).name("Node A").build());
        graph.addNode(KnowledgeNode.builder().id(nodeB).name("Node B").build());

        KnowledgeEdge edge = KnowledgeEdge.create(nodeA, nodeB, RelationshipType.DEPENDS_ON, 0.85);
        graph.addEdge(edge);

        assertThat(graph.getEdge(edge.getId())).isNotNull();
        assertThat(graph.getEdges()).hasSize(1);
        assertThat(graph.getOutgoingEdges(nodeA)).hasSize(1);
        assertThat(graph.getIncomingEdges(nodeB)).hasSize(1);
        assertThat(graph.getAdjacentNodes(nodeA)).hasSize(1);

        graph.removeEdge(edge.getId());
        assertThat(graph.getEdge(edge.getId())).isNull();
        assertThat(graph.getEdges()).isEmpty();
    }

    @Test
    @DisplayName("Should merge node attributes and metadata for existing nodes")
    void testNodeMerging() {
        NodeIdentifier nodeId = NodeIdentifier.of(NodeType.PERSON, "P123");
        KnowledgeNode initialNode = KnowledgeNode.builder()
                .id(nodeId)
                .type(NodeType.PERSON)
                .name("Prof. Smith")
                .attributes(NodeAttributes.of(Map.of("role", "Professor")))
                .build();

        graph.addNode(initialNode);

        KnowledgeNode updatedNode = KnowledgeNode.builder()
                .id(nodeId)
                .type(NodeType.PERSON)
                .name("Prof. John Smith")
                .attributes(NodeAttributes.of(Map.of("department", "Computer Science")))
                .build();

        boolean merged = graph.mergeNode(updatedNode);
        assertThat(merged).isTrue();

        KnowledgeNode retrieved = graph.getNode(nodeId);
        assertThat(retrieved.getName()).isEqualTo("Prof. John Smith");
        assertThat(retrieved.getAttributes().getString("role")).isEqualTo("Professor");
        assertThat(retrieved.getAttributes().getString("department")).isEqualTo("Computer Science");
    }

    @Test
    @DisplayName("Should deduplicate identical edges by combining strengths")
    void testEdgeDeduplication() {
        NodeIdentifier nodeA = NodeIdentifier.of("node_a");
        NodeIdentifier nodeB = NodeIdentifier.of("node_b");
        graph.addNode(KnowledgeNode.builder().id(nodeA).name("A").build());
        graph.addNode(KnowledgeNode.builder().id(nodeB).name("B").build());

        KnowledgeEdge edge1 = KnowledgeEdge.create(nodeA, nodeB, RelationshipType.RELATED_TO, 0.40);
        KnowledgeEdge edge2 = KnowledgeEdge.create(nodeA, nodeB, RelationshipType.RELATED_TO, 0.80);

        graph.addEdge(edge1);
        boolean deduplicated = graph.deduplicateEdge(edge2);

        assertThat(deduplicated).isTrue();
        assertThat(graph.getEdges()).hasSize(1);
        assertThat(graph.getEdge(edge1.getId()).getStrength().getScore()).isEqualTo(0.80);
    }

    @Test
    @DisplayName("Should clean dangling edges during consistency validation")
    void testConsistencyValidation() {
        NodeIdentifier nodeA = NodeIdentifier.of("node_a");
        NodeIdentifier nodeB = NodeIdentifier.of("node_b");
        graph.addNode(KnowledgeNode.builder().id(nodeA).name("A").build());
        // nodeB is NOT added to graph

        KnowledgeEdge danglingEdge = KnowledgeEdge.create(nodeA, nodeB, RelationshipType.REFERENCES);
        graph.addEdge(danglingEdge);

        int removed = graph.validateConsistency();
        assertThat(removed).isEqualTo(1);
        assertThat(graph.getEdges()).isEmpty();
    }
}
