package com.campusguide.personal.ai.atlas.orchestration.memory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Versioned entry stored in Atlas Shared Memory.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedState {

    private String namespace;
    private String key;
    private Object value;
    @Builder.Default
    private long version = 1L;
    private String lastModifiedBy;
    @Builder.Default
    private Instant lastModifiedAt = Instant.now();
    private MemoryLease activeLease;

    public static SharedState of(String namespace, String key, Object value, String agentId) {
        return SharedState.builder()
                .namespace(namespace)
                .key(key)
                .value(value)
                .version(1L)
                .lastModifiedBy(agentId)
                .lastModifiedAt(Instant.now())
                .build();
    }

    public void update(Object newValue, String agentId) {
        this.value = newValue;
        this.version++;
        this.lastModifiedBy = agentId;
        this.lastModifiedAt = Instant.now();
    }
}
