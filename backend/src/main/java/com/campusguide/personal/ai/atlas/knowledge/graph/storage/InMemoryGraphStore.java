package com.campusguide.personal.ai.atlas.knowledge.graph.storage;

import com.campusguide.personal.ai.atlas.knowledge.graph.model.KnowledgeGraph;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe In-Memory implementation of GraphStore.
 */
@Component
public class InMemoryGraphStore implements GraphStore {

    private final Map<String, KnowledgeGraph> store = new ConcurrentHashMap<>();
    private final Map<String, List<GraphSnapshot>> snapshotStore = new ConcurrentHashMap<>();

    @Override
    public void save(KnowledgeGraph graph) {
        if (graph != null && graph.getMetadata() != null && graph.getMetadata().getGraphId() != null) {
            store.put(graph.getMetadata().getGraphId(), graph);
        }
    }

    @Override
    public Optional<KnowledgeGraph> findById(String graphId) {
        if (graphId == null) return Optional.empty();
        return Optional.ofNullable(store.get(graphId));
    }

    @Override
    public List<KnowledgeGraph> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public boolean deleteById(String graphId) {
        if (graphId == null) return false;
        snapshotStore.remove(graphId);
        return store.remove(graphId) != null;
    }

    @Override
    public boolean existsById(String graphId) {
        return graphId != null && store.containsKey(graphId);
    }

    @Override
    public Optional<GraphSnapshot> createSnapshot(String graphId) {
        KnowledgeGraph graph = store.get(graphId);
        if (graph == null) return Optional.empty();

        GraphSnapshot snapshot = GraphSnapshot.fromGraph(graph);
        snapshotStore.computeIfAbsent(graphId, k -> new ArrayList<>()).add(snapshot);
        return Optional.of(snapshot);
    }

    @Override
    public KnowledgeGraph restoreSnapshot(GraphSnapshot snapshot) {
        if (snapshot == null) throw new IllegalArgumentException("GraphSnapshot cannot be null");
        KnowledgeGraph restored = snapshot.toGraph();
        save(restored);
        return restored;
    }
}
