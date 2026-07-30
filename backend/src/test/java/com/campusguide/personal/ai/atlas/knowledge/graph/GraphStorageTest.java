package com.campusguide.personal.ai.atlas.knowledge.graph;

import com.campusguide.personal.ai.atlas.knowledge.graph.model.KnowledgeGraph;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeIdentifier;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.KnowledgeNode;
import com.campusguide.personal.ai.atlas.knowledge.graph.storage.GraphRepository;
import com.campusguide.personal.ai.atlas.knowledge.graph.storage.GraphSnapshot;
import com.campusguide.personal.ai.atlas.knowledge.graph.storage.InMemoryGraphStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class GraphStorageTest {

    private InMemoryGraphStore graphStore;
    private GraphRepository repository;

    @BeforeEach
    void setUp() {
        graphStore = new InMemoryGraphStore();
        repository = new GraphRepository(graphStore);
    }

    @Test
    @DisplayName("Should save, find, and delete graphs cleanly")
    void testGraphStorageLifecycle() {
        KnowledgeGraph graph = KnowledgeGraph.create("store_graph_1", "Storage Graph 1");
        NodeIdentifier node1 = NodeIdentifier.of("node1");
        graph.addNode(KnowledgeNode.builder().id(node1).name("Node 1").build());

        repository.save(graph);

        assertThat(repository.existsById("store_graph_1")).isTrue();

        Optional<KnowledgeGraph> found = repository.findById("store_graph_1");
        assertThat(found).isPresent();
        assertThat(found.get().getNode(node1)).isNotNull();

        boolean deleted = repository.deleteById("store_graph_1");
        assertThat(deleted).isTrue();
        assertThat(repository.existsById("store_graph_1")).isFalse();
    }

    @Test
    @DisplayName("Should create snapshot and restore graph successfully")
    void testSnapshotAndRestore() {
        KnowledgeGraph graph = KnowledgeGraph.create("snap_graph", "Snapshot Graph");
        NodeIdentifier node1 = NodeIdentifier.of("node1");
        graph.addNode(KnowledgeNode.builder().id(node1).name("Node 1").build());
        repository.save(graph);

        Optional<GraphSnapshot> snapshotOpt = repository.createSnapshot("snap_graph");
        assertThat(snapshotOpt).isPresent();

        GraphSnapshot snapshot = snapshotOpt.get();
        assertThat(snapshot.getGraphId()).isEqualTo("snap_graph");
        assertThat(snapshot.getNodes()).hasSize(1);

        // Delete original from store
        repository.deleteById("snap_graph");
        assertThat(repository.existsById("snap_graph")).isFalse();

        // Restore from snapshot
        KnowledgeGraph restored = repository.restoreSnapshot(snapshot);
        assertThat(restored).isNotNull();
        assertThat(repository.existsById("snap_graph")).isTrue();
        assertThat(repository.findById("snap_graph").get().getNode(node1)).isNotNull();
    }
}
