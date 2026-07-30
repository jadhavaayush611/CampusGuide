package com.campusguide.personal.ai.atlas.execution.runtime.human;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Control service handling human interactions, manual approvals, pauses, resumes, cancellations, and manual overrides.
 */
@Slf4j
@Service
public class ExecutionControlService {

    private final Map<String, ApprovalWaitState> pendingApprovals = new ConcurrentHashMap<>();
    private final List<ManualIntervention> interventionHistory = Collections.synchronizedList(new ArrayList<>());

    public ApprovalWaitState registerWaitState(String workflowId, String instanceId, String unitId, String prompt, long timeoutSeconds) {
        ApprovalWaitState waitState = ApprovalWaitState.builder()
                .workflowId(workflowId)
                .instanceId(instanceId)
                .unitId(unitId)
                .promptMessage(prompt)
                .timeoutSeconds(timeoutSeconds)
                .status(ApprovalWaitState.Status.PENDING)
                .build();

        pendingApprovals.put(waitState.getWaitId(), waitState);
        log.info("Registered approval wait state {} for instance {} unit {}", waitState.getWaitId(), instanceId, unitId);
        return waitState;
    }

    public boolean submitApproval(String waitId, boolean approved, String operatorId, String notes) {
        ApprovalWaitState waitState = pendingApprovals.get(waitId);
        if (waitState == null || waitState.getStatus() != ApprovalWaitState.Status.PENDING) {
            log.warn("Approval wait state {} not found or not PENDING", waitId);
            return false;
        }

        waitState.setStatus(approved ? ApprovalWaitState.Status.APPROVED : ApprovalWaitState.Status.REJECTED);
        waitState.setResolvedBy(operatorId);
        waitState.setResolvedAt(Instant.now());

        ManualIntervention intervention = ManualIntervention.builder()
                .workflowId(waitState.getWorkflowId())
                .instanceId(waitState.getInstanceId())
                .unitId(waitState.getUnitId())
                .interventionType("APPROVAL")
                .operatorId(operatorId)
                .outcome(approved ? "APPROVED" : "REJECTED")
                .notes(notes)
                .build();

        interventionHistory.add(intervention);
        log.info("Approval {} resolved by {} as {}", waitId, operatorId, waitState.getStatus());
        return true;
    }

    public void recordIntervention(String workflowId, String instanceId, String unitId, String type, String operatorId, String outcome, String notes) {
        ManualIntervention intervention = ManualIntervention.builder()
                .workflowId(workflowId)
                .instanceId(instanceId)
                .unitId(unitId)
                .interventionType(type)
                .operatorId(operatorId)
                .outcome(outcome)
                .notes(notes)
                .build();
        interventionHistory.add(intervention);
    }

    public Optional<ApprovalWaitState> getWaitState(String waitId) {
        return Optional.ofNullable(pendingApprovals.get(waitId));
    }

    public List<ApprovalWaitState> getPendingApprovals() {
        List<ApprovalWaitState> list = new ArrayList<>();
        for (ApprovalWaitState state : pendingApprovals.values()) {
            if (state.getStatus() == ApprovalWaitState.Status.PENDING) {
                list.add(state);
            }
        }
        return list;
    }

    public List<ManualIntervention> getInterventionHistory() {
        return new ArrayList<>(interventionHistory);
    }
}
