package com.campusguide.personal.ai.atlas.orchestration.memory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe shared memory storage for multi-agent execution state.
 */
@Slf4j
@Component
public class SharedMemory {

    // Map of namespace -> (key -> SharedState)
    private final Map<String, Map<String, SharedState>> memoryStore = new ConcurrentHashMap<>();

    public void put(String namespace, String key, Object value, String agentId) {
        if (namespace == null || key == null) return;

        memoryStore.computeIfAbsent(namespace, k -> new ConcurrentHashMap<>())
                .compute(key, (k, existingState) -> {
                    if (existingState == null) {
                        return SharedState.of(namespace, key, value, agentId);
                    } else {
                        existingState.update(value, agentId);
                        return existingState;
                    }
                });
        log.debug("SharedMemory put [namespace={}, key={}, agent={}]", namespace, key, agentId);
    }

    public Optional<SharedState> get(String namespace, String key) {
        if (namespace == null || key == null) return Optional.empty();
        Map<String, SharedState> nsMap = memoryStore.get(namespace);
        if (nsMap == null) return Optional.empty();
        return Optional.ofNullable(nsMap.get(key));
    }

    public Map<String, SharedState> getNamespace(String namespace) {
        if (namespace == null) return Map.of();
        Map<String, SharedState> nsMap = memoryStore.get(namespace);
        return nsMap != null ? new ConcurrentHashMap<>(nsMap) : Map.of();
    }

    public boolean remove(String namespace, String key) {
        if (namespace == null || key == null) return false;
        Map<String, SharedState> nsMap = memoryStore.get(namespace);
        if (nsMap != null) {
            return nsMap.remove(key) != null;
        }
        return false;
    }

    public void clearNamespace(String namespace) {
        if (namespace != null) {
            memoryStore.remove(namespace);
        }
    }
}
