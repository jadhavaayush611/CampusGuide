package com.campusguide.personal.ai.atlas.knowledge.graph.registry;

import com.campusguide.personal.ai.atlas.knowledge.graph.lifecycle.GraphLifecycleState;
import com.campusguide.personal.ai.atlas.knowledge.graph.model.KnowledgeGraph;
import com.campusguide.personal.ai.atlas.knowledge.graph.storage.GraphRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Global registry for tracking Knowledge Graph catalog metadata, lifecycle, versions, counts, and history.
 */
@Service
public class KnowledgeGraphRegistry {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeGraphRegistry.class);

    private final GraphRepository graphRepository;
    private final Map<String, GraphCatalogEntry> catalog = new ConcurrentHashMap<>();

    public KnowledgeGraphRegistry(GraphRepository graphRepository) {
        this.graphRepository = graphRepository;
        refreshCatalog();
    }

    public synchronized void registerGraph(KnowledgeGraph graph) {
        if (graph == null || graph.getMetadata() == null) {
            return;
        }

        graphRepository.save(graph);
        GraphCatalogEntry entry = GraphCatalogEntry.fromGraph(graph);
        if (entry != null) {
            catalog.put(entry.getGraphId(), entry);
            log.info("Registered KnowledgeGraph in registry. GraphId: {}, State: {}, Nodes: {}, Edges: {}",
                    entry.getGraphId(), entry.getLifecycleState(), entry.getNodeCount(), entry.getEdgeCount());
        }
    }

    public Optional<KnowledgeGraph> getGraph(String graphId) {
        return graphRepository.findById(graphId);
    }

    public Optional<GraphCatalogEntry> getCatalogEntry(String graphId) {
        if (graphId == null) return Optional.empty();
        GraphCatalogEntry entry = catalog.get(graphId);
        if (entry == null) {
            // Attempt to load from repository
            Optional<KnowledgeGraph> graphOpt = graphRepository.findById(graphId);
            if (graphOpt.isPresent()) {
                entry = GraphCatalogEntry.fromGraph(graphOpt.get());
                if (entry != null) catalog.put(graphId, entry);
            }
        }
        return Optional.ofNullable(entry);
    }

    public boolean updateLifecycleState(String graphId, GraphLifecycleState newState) {
        if (graphId == null || newState == null) return false;

        Optional<KnowledgeGraph> graphOpt = graphRepository.findById(graphId);
        if (graphOpt.isPresent()) {
            KnowledgeGraph graph = graphOpt.get();
            graph.getMetadata().setLifecycleState(newState);
            graph.getMetadata().setUpdatedAt(java.time.Instant.now());
            registerGraph(graph);
            return true;
        }
        return false;
    }

    public List<GraphCatalogEntry> listCatalog() {
        refreshCatalog();
        return new ArrayList<>(catalog.values());
    }

    public List<GraphCatalogEntry> listCatalogByLifecycle(GraphLifecycleState state) {
        if (state == null) return List.of();
        return listCatalog().stream()
                .filter(e -> e.getLifecycleState() == state)
                .collect(Collectors.toList());
    }

    public boolean unregisterGraph(String graphId) {
        if (graphId == null) return false;
        catalog.remove(graphId);
        return graphRepository.deleteById(graphId);
    }

    public void refreshCatalog() {
        List<KnowledgeGraph> allGraphs = graphRepository.findAll();
        catalog.clear();
        for (KnowledgeGraph g : allGraphs) {
            GraphCatalogEntry entry = GraphCatalogEntry.fromGraph(g);
            if (entry != null) {
                catalog.put(entry.getGraphId(), entry);
            }
        }
    }
}
