package com.campusguide.personal.ai.atlas.orchestration.persistence;

import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowInstance;
import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowRuntime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service managing resumption of suspended long-running workflows.
 * Verifies leases, restores execution context state, and resumes Execution Runtime execution.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeCoordinator {

    private final WorkflowPersistence persistence;
    private final WorkflowRuntime workflowRuntime;

    public Optional<WorkflowInstance> resumeWorkflow(String workflowId, String agentId) {
        if (workflowId == null) return Optional.empty();

        Optional<WorkflowPersistence.WorkflowSnapshot> snapshotOpt = persistence.getSnapshot(workflowId);
        if (snapshotOpt.isEmpty()) {
            log.warn("Cannot resume workflow {}: Snapshot not found in persistence", workflowId);
            return Optional.empty();
        }

        WorkflowPersistence.WorkflowSnapshot snapshot = snapshotOpt.get();
        WorkflowLease lease = snapshot.getLease();
        if (lease == null || lease.isExpired()) {
            log.warn("Renewing expired/missing lease for workflow {}", workflowId);
            lease = WorkflowLease.grant(workflowId, agentId, 86400000L);
            snapshot.setLease(lease);
            persistence.saveLease(lease);
        } else {
            lease.renew(86400000L);
        }

        log.info("ResumeCoordinator resuming workflow instance {} for workflow {}", snapshot.getInstanceId(), workflowId);

        workflowRuntime.resumeWorkflow(snapshot.getInstanceId());
        WorkflowInstance instance = workflowRuntime.getInstance(snapshot.getInstanceId());

        return Optional.ofNullable(instance);
    }
}
