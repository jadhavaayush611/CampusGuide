package com.campusguide.personal.ai.atlas.orchestration.persistence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Lease model guaranteeing exclusive execution ownership for long-running workflows.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowLease {

    private String leaseId;
    private String workflowId;
    private String assignedAgentId;
    @Builder.Default
    private Instant grantedAt = Instant.now();
    private Instant expiresAt;
    @Builder.Default
    private boolean active = true;

    public static WorkflowLease grant(String workflowId, String agentId, long durationMs) {
        Instant now = Instant.now();
        return WorkflowLease.builder()
                .leaseId("wlease_" + UUID.randomUUID().toString().substring(0, 8))
                .workflowId(workflowId)
                .assignedAgentId(agentId)
                .grantedAt(now)
                .expiresAt(now.plusMillis(durationMs))
                .active(true)
                .build();
    }

    public boolean isExpired() {
        return !active || (expiresAt != null && Instant.now().isAfter(expiresAt));
    }

    public void renew(long durationMs) {
        this.expiresAt = Instant.now().plusMillis(durationMs);
        this.active = true;
    }

    public void release() {
        this.active = false;
    }
}
