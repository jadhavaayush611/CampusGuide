package com.campusguide.personal.ai.atlas.orchestration.persistence;

import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowInstance;
import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowRuntime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * Service managing suspension of long-running workflows lasting hours, days, or weeks.
 * Captures execution snapshots, releases live agent locks, and persists workflow state.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SuspensionManager {

    private final WorkflowPersistence persistence;
    private final WorkflowRuntime workflowRuntime;

    public boolean suspendWorkflow(String instanceId, String reason, String agentId) {
        if (instanceId == null) return false;

        WorkflowInstance instance = workflowRuntime.getInstance(instanceId);
        if (instance == null) {
            log.warn("Cannot suspend workflow instance {}: Not found in WorkflowRuntime", instanceId);
            return false;
        }

        workflowRuntime.pauseWorkflow(instanceId, reason);

        WorkflowLease lease = persistence.getLease(instance.getWorkflowId())
                .orElse(WorkflowLease.grant(instance.getWorkflowId(), agentId, 86400000L)); // 24hr default lease

        WorkflowPersistence.WorkflowSnapshot snapshot = WorkflowPersistence.WorkflowSnapshot.builder()
                .snapshotId("snap_" + UUID.randomUUID().toString().substring(0, 8))
                .workflowId(instance.getWorkflowId())
                .instanceId(instanceId)
                .instance(instance)
                .lease(lease)
                .suspensionReason(reason)
                .stateData(instance.getSession() != null ? instance.getSession().getVariables() : Collections.emptyMap())
                .build();

        persistence.saveSnapshot(snapshot);
        log.info("SuspensionManager successfully suspended long-running workflow instance {} (Reason: {})", instanceId, reason);
        return true;
    }
}
