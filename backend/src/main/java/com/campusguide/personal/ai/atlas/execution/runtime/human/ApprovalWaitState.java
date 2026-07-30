package com.campusguide.personal.ai.atlas.execution.runtime.human;

import com.campusguide.personal.ai.atlas.execution.approval.ApprovalRequirement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * State representing a workflow instance waiting for manual human approval.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalWaitState implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Status {
        PENDING,
        APPROVED,
        REJECTED,
        TIMED_OUT
    }

    @Builder.Default
    private String waitId = "wait_" + UUID.randomUUID().toString().substring(0, 8);

    private String workflowId;
    private String instanceId;
    private String unitId;
    private ApprovalRequirement approvalRequirement;
    private String requiredRole;
    private String promptMessage;
    private long timeoutSeconds;

    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private Status status = Status.PENDING;

    private String resolvedBy;
    private Instant resolvedAt;
}
