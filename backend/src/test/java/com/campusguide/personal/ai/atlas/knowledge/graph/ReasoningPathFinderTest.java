package com.campusguide.personal.ai.atlas.knowledge.graph;

import com.campusguide.personal.ai.atlas.knowledge.graph.edge.KnowledgeEdge;
import com.campusguide.personal.ai.atlas.knowledge.graph.edge.RelationshipStrength;
import com.campusguide.personal.ai.atlas.knowledge.graph.edge.RelationshipType;
import com.campusguide.personal.ai.atlas.knowledge.graph.model.KnowledgeGraph;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.KnowledgeNode;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeIdentifier;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeType;
import com.campusguide.personal.ai.atlas.knowledge.graph.path.EvidencePath;
import com.campusguide.personal.ai.atlas.knowledge.graph.path.ReasoningPathFinder;
import com.campusguide.personal.ai.atlas.knowledge.graph.projection.GraphProjectionBuilder;
import com.campusguide.personal.ai.atlas.knowledge.graph.view.KnowledgeGraphView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReasoningPathFinderTest {

    private KnowledgeGraph graph;
    private KnowledgeNode nodeA;
    private KnowledgeNode nodeB;
    private KnowledgeNode nodeC;

    @BeforeEach
    void setUp() {
        graph = KnowledgeGraph.create("path_graph");
        nodeA = KnowledgeNode.builder().id(NodeIdentifier.of(NodeType.COURSE, "CS101")).name("CS101").type(NodeType.COURSE).sourceCollectionId("cs").build();
        nodeB = KnowledgeNode.builder().id(NodeIdentifier.of(NodeType.COURSE, "CS102")).name("CS102").type(NodeType.COURSE).sourceCollectionId("cs").build();
        nodeC = KnowledgeNode.builder().id(NodeIdentifier.of(NodeType.COURSE, "CS201")).name("CS201").type(NodeType.COURSE).sourceCollectionId("cs").build();

        KnowledgeEdge e1 = KnowledgeEdge.builder().id("e1").sourceNodeId(nodeA.getId()).targetNodeId(nodeB.getId()).relationshipType(RelationshipType.PREREQUISITE_FOR).strength(RelationshipStrength.of(0.9)).build();
        KnowledgeEdge e2 = KnowledgeEdge.builder().id("e2").sourceNodeId(nodeB.getId()).targetNodeId(nodeC.getId()).relationshipType(RelationshipType.PREREQUISITE_FOR).strength(RelationshipStrength.of(0.8)).build();

        graph.addNode(nodeA);
        graph.addNode(nodeB);
        graph.addNode(nodeC);
        graph.addEdge(e1);
        graph.addEdge(e2);
    }

    @Test
    @DisplayName("ReasoningPathFinder discovers multi-hop paths across KnowledgeGraphView")
    void testFindPaths() {
        KnowledgeGraphView view = new GraphProjectionBuilder().buildFrom(graph);
        ReasoningPathFinder pathFinder = new ReasoningPathFinder();

        List<EvidencePath> paths = pathFinder.findPaths(
                view,
                Collections.emptyList(),
                nodeA.getId(),
                nodeC.getId(),
                ReasoningPathFinder.PathStrategy.SHORTEST,
                4,
                Collections.emptySet()
        );

        assertNotNull(paths);
        assertFalse(paths.isEmpty());
        EvidencePath primaryPath = paths.get(0);
        assertEquals(2, primaryPath.getHopCount());
        assertEquals(nodeA.getId().getValue(), primaryPath.getSourceNodeId());
        assertEquals(nodeC.getId().getValue(), primaryPath.getTargetNodeId());
    }
}
