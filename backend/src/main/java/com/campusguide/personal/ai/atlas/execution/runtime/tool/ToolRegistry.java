package com.campusguide.personal.ai.atlas.execution.runtime.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe registry for provider-independent ToolAdapter implementations.
 */
@Slf4j
@Component
public class ToolRegistry {

    private final List<ToolAdapter> adapters = new CopyOnWriteArrayList<>();

    public ToolRegistry(List<ToolAdapter> initialAdapters) {
        if (initialAdapters != null) {
            adapters.addAll(initialAdapters);
        }
    }

    public void registerAdapter(ToolAdapter adapter) {
        if (adapter != null && !adapters.contains(adapter)) {
            adapters.add(0, adapter); // Add at front for higher priority override
            log.info("Registered ToolAdapter: {}", adapter.getClass().getSimpleName());
        }
    }

    public ToolAdapter resolveAdapter(String capability) {
        if (capability != null) {
            for (ToolAdapter adapter : adapters) {
                if (adapter.supports(capability)) {
                    return adapter;
                }
            }
        }
        log.debug("No specific adapter registered for capability '{}', using fallback InternalServiceToolAdapter", capability);
        return new InternalServiceToolAdapter();
    }

    public List<ToolAdapter> getAllAdapters() {
        return new ArrayList<>(adapters);
    }
}
