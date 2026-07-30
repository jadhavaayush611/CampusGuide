package com.campusguide.personal.ai.atlas.knowledge.graph;

import com.campusguide.personal.ai.atlas.knowledge.graph.lifecycle.GraphLifecycleState;
import com.campusguide.personal.ai.atlas.knowledge.graph.model.KnowledgeGraph;
import com.campusguide.personal.ai.atlas.knowledge.graph.registry.GraphCatalogEntry;
import com.campusguide.personal.ai.atlas.knowledge.graph.registry.KnowledgeGraphRegistry;
import com.campusguide.personal.ai.atlas.knowledge.graph.storage.GraphRepository;
import com.campusguide.personal.ai.atlas.knowledge.graph.storage.InMemoryGraphStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class KnowledgeGraphRegistryTest {

    private KnowledgeGraphRegistry registry;

    @BeforeEach
    void setUp() {
        InMemoryGraphStore store = new InMemoryGraphStore();
        GraphRepository repository = new GraphRepository(store);
        registry = new KnowledgeGraphRegistry(repository);
    }

    @Test
    @DisplayName("Should register graph, maintain catalog entry, and track lifecycle state")
    void testRegistryLifecycleTracking() {
        KnowledgeGraph graph = KnowledgeGraph.create("reg_graph_1", "Registry Graph");
        graph.getMetadata().setLifecycleState(GraphLifecycleState.BUILDING);

        registry.registerGraph(graph);

        Optional<GraphCatalogEntry> entryOpt = registry.getCatalogEntry("reg_graph_1");
        assertThat(entryOpt).isPresent();
        assertThat(entryOpt.get().getLifecycleState()).isEqualTo(GraphLifecycleState.BUILDING);

        boolean updated = registry.updateLifecycleState("reg_graph_1", GraphLifecycleState.ACTIVE);
        assertThat(updated).isTrue();

        Optional<GraphCatalogEntry> updatedEntryOpt = registry.getCatalogEntry("reg_graph_1");
        assertThat(updatedEntryOpt).isPresent();
        assertThat(updatedEntryOpt.get().getLifecycleState()).isEqualTo(GraphLifecycleState.ACTIVE);

        List<GraphCatalogEntry> activeList = registry.listCatalogByLifecycle(GraphLifecycleState.ACTIVE);
        assertThat(activeList).hasSize(1);
    }
}
