package com.campusguide.personal.ai.atlas.orchestration.delegation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Task assignment record connecting a task to a delegated agent.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskAssignment {

    private String assignmentId;
    private String taskId;
    private String agentId;
    @Builder.Default
    private Instant assignedAt = Instant.now();
    @Builder.Default
    private AssignmentStatus status = AssignmentStatus.ASSIGNED;
    @Builder.Default
    private int priority = 5;
    private String localityKey;
    private String explanation;

    @Builder.Default
    private Map<String, Object> executionParams = new HashMap<>();

    public enum AssignmentStatus {
        PENDING,
        ASSIGNED,
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        CANCELLED
    }

    public static TaskAssignment create(String taskId, String agentId, int priority, String localityKey, String explanation) {
        return TaskAssignment.builder()
                .assignmentId("assign_" + UUID.randomUUID().toString().substring(0, 8))
                .taskId(taskId)
                .agentId(agentId)
                .assignedAt(Instant.now())
                .status(AssignmentStatus.ASSIGNED)
                .priority(priority)
                .localityKey(localityKey)
                .explanation(explanation)
                .build();
    }
}
