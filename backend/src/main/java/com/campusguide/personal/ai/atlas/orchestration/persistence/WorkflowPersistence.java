package com.campusguide.personal.ai.atlas.orchestration.persistence;

import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowInstance;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory persistence store for long-running workflow snapshots and state persistence.
 */
@Slf4j
@Component
public class WorkflowPersistence {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowSnapshot {
        private String snapshotId;
        private String workflowId;
        private String instanceId;
        private WorkflowInstance instance;
        private WorkflowLease lease;
        private String suspensionReason;
        @Builder.Default
        private Instant suspendedAt = Instant.now();
        private Map<String, Object> stateData;
    }

    private final Map<String, WorkflowSnapshot> snapshots = new ConcurrentHashMap<>();
    private final Map<String, WorkflowLease> activeLeases = new ConcurrentHashMap<>();

    public void saveSnapshot(WorkflowSnapshot snapshot) {
        if (snapshot == null || snapshot.getWorkflowId() == null) return;
        snapshots.put(snapshot.getWorkflowId(), snapshot);
        if (snapshot.getLease() != null) {
            activeLeases.put(snapshot.getWorkflowId(), snapshot.getLease());
        }
        log.info("WorkflowPersistence saved snapshot for long-running workflow {}", snapshot.getWorkflowId());
    }

    public Optional<WorkflowSnapshot> getSnapshot(String workflowId) {
        if (workflowId == null) return Optional.empty();
        return Optional.ofNullable(snapshots.get(workflowId));
    }

    public Optional<WorkflowLease> getLease(String workflowId) {
        if (workflowId == null) return Optional.empty();
        return Optional.ofNullable(activeLeases.get(workflowId));
    }

    public void saveLease(WorkflowLease lease) {
        if (lease != null && lease.getWorkflowId() != null) {
            activeLeases.put(lease.getWorkflowId(), lease);
        }
    }

    public void removeSnapshot(String workflowId) {
        if (workflowId != null) {
            snapshots.remove(workflowId);
            activeLeases.remove(workflowId);
        }
    }
}
