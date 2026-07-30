package com.campusguide.personal.ai.atlas.orchestration.memory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Memory lease token ensuring exclusive or synchronized access to shared state.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemoryLease {

    private String leaseId;
    private String namespace;
    private String key;
    private String holderAgentId;
    @Builder.Default
    private Instant acquiredAt = Instant.now();
    private Instant expiresAt;
    @Builder.Default
    private boolean active = true;

    public static MemoryLease create(String namespace, String key, String agentId, long durationMs) {
        Instant now = Instant.now();
        return MemoryLease.builder()
                .leaseId("lease_" + UUID.randomUUID().toString().substring(0, 8))
                .namespace(namespace)
                .key(key)
                .holderAgentId(agentId)
                .acquiredAt(now)
                .expiresAt(now.plusMillis(durationMs))
                .active(true)
                .build();
    }

    public boolean isExpired() {
        return !active || (expiresAt != null && Instant.now().isAfter(expiresAt));
    }

    public void release() {
        this.active = false;
    }
}
