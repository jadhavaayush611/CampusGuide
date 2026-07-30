package com.campusguide.personal.ai.atlas.orchestration.workflow;

import com.campusguide.personal.ai.atlas.execution.model.ExecutionStage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Sub-workflow partition created for distributed multi-agent execution.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowPartition {

    private String partitionId;
    private String parentWorkflowId;
    private int orderIndex;
    @Builder.Default
    private List<ExecutionStage> stages = new ArrayList<>();
    @Builder.Default
    private List<String> dependencyPartitionIds = new ArrayList<>();
    private String assignedAgentId;
    @Builder.Default
    private PartitionStatus status = PartitionStatus.PENDING;
    @Builder.Default
    private Map<String, Object> resultData = new HashMap<>();

    public enum PartitionStatus {
        PENDING,
        ASSIGNED,
        RUNNING,
        COMPLETED,
        FAILED
    }

    public static WorkflowPartition create(String parentWorkflowId, int orderIndex, List<ExecutionStage> stages) {
        return WorkflowPartition.builder()
                .partitionId("partition_" + UUID.randomUUID().toString().substring(0, 8))
                .parentWorkflowId(parentWorkflowId)
                .orderIndex(orderIndex)
                .stages(stages != null ? stages : new ArrayList<>())
                .status(PartitionStatus.PENDING)
                .build();
    }
}
