package com.campusguide.personal.ai.atlas.orchestration.workflow;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionStage;
import com.campusguide.personal.ai.atlas.execution.runtime.engine.ExecutionRuntime;
import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowInstance;
import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowState;
import com.campusguide.personal.ai.atlas.orchestration.agent.AgentRuntime;
import com.campusguide.personal.ai.atlas.orchestration.delegation.DelegationEngine;
import com.campusguide.personal.ai.atlas.orchestration.delegation.DelegationPolicy;
import com.campusguide.personal.ai.atlas.orchestration.delegation.TaskAssignment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for partitioning workflows and executing them across distributed specialized agents over the Execution Runtime.
 *
 * STRICT INVARIANT:
 * Reuses existing Execution Runtime engine and does NOT duplicate execution logic.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DistributedWorkflowExecutor {

    private final ExecutionRuntime executionRuntime;
    private final DelegationEngine delegationEngine;
    private final AgentRuntime agentRuntime;
    private final ResultMerger resultMerger;

    public WorkflowInstance executeDistributed(ExecutionContext context, ExecutableWorkflow workflow, PartitionStrategy strategy) {
        if (workflow == null) {
            log.error("Cannot execute null ExecutableWorkflow");
            return null;
        }

        log.info("Starting distributed workflow execution for workflowId {} using strategy {}", workflow.getWorkflowId(), strategy);

        List<WorkflowPartition> partitions = partitionWorkflow(workflow, strategy != null ? strategy : PartitionStrategy.STAGE_BASED);
        List<WorkflowPartition> sortedPartitions = partitions.stream()
                .sorted(Comparator.comparingInt(WorkflowPartition::getOrderIndex))
                .collect(Collectors.toList());

        List<WorkflowPartition> executedPartitions = new ArrayList<>();
        boolean overallSuccess = true;
        WorkflowInstance finalInstance = null;

        for (WorkflowPartition partition : sortedPartitions) {
            // Find required capability from stages if present
            String requiredCap = extractRequiredCapability(partition);
            Optional<TaskAssignment> assignmentOpt = delegationEngine.delegateTask(
                    partition.getPartitionId(), requiredCap, 5, null, DelegationPolicy.defaultPolicy());

            if (assignmentOpt.isEmpty()) {
                log.error("Failed to assign agent for partition {}", partition.getPartitionId());
                partition.setStatus(WorkflowPartition.PartitionStatus.FAILED);
                overallSuccess = false;
                break;
            }

            TaskAssignment assignment = assignmentOpt.get();
            partition.setAssignedAgentId(assignment.getAgentId());
            partition.setStatus(WorkflowPartition.PartitionStatus.ASSIGNED);

            ExecutableWorkflow partitionSubWorkflow = ExecutableWorkflow.builder()
                    .workflowId(partition.getPartitionId())
                    .planId(workflow.getPlanId())
                    .stages(partition.getStages())
                    .build();

            log.info("Executing partition {} via agent {}", partition.getPartitionId(), assignment.getAgentId());
            partition.setStatus(WorkflowPartition.PartitionStatus.RUNNING);

            WorkflowInstance partitionInstance = agentRuntime.executeDelegatedTask(assignment.getAgentId(), context, partitionSubWorkflow);
            finalInstance = partitionInstance;

            if (partitionInstance != null && partitionInstance.getState() == WorkflowState.COMPLETED) {
                partition.setStatus(WorkflowPartition.PartitionStatus.COMPLETED);
                if (partitionInstance.getSession() != null && partitionInstance.getSession().getVariables() != null) {
                    partition.getResultData().putAll(partitionInstance.getSession().getVariables());
                }
                executedPartitions.add(partition);
            } else {
                log.error("Partition {} execution failed", partition.getPartitionId());
                partition.setStatus(WorkflowPartition.PartitionStatus.FAILED);
                overallSuccess = false;
                break;
            }
        }

        if (finalInstance == null) {
            // Fallback direct execution via ExecutionRuntime if no partition executed
            return executionRuntime.executeWorkflow(context, workflow);
        }

        if (overallSuccess) {
            resultMerger.applyToInstance(finalInstance, executedPartitions);
            log.info("Successfully completed distributed execution for workflow {}", workflow.getWorkflowId());
        } else {
            log.warn("Distributed execution for workflow {} completed with failures", workflow.getWorkflowId());
        }

        return finalInstance;
    }

    public List<WorkflowPartition> partitionWorkflow(ExecutableWorkflow workflow, PartitionStrategy strategy) {
        List<WorkflowPartition> partitions = new ArrayList<>();
        if (workflow == null || workflow.getStages() == null || workflow.getStages().isEmpty()) {
            return partitions;
        }

        List<ExecutionStage> stages = workflow.getStages();
        if (strategy == PartitionStrategy.STAGE_BASED) {
            for (int i = 0; i < stages.size(); i++) {
                partitions.add(WorkflowPartition.create(workflow.getWorkflowId(), i, List.of(stages.get(i))));
            }
        } else {
            // BALANCED / CAPABILITY_BASED / DATA_LOCALITY: Single partition if stages <= 2, else group by pairs
            for (int i = 0; i < stages.size(); i += 2) {
                int end = Math.min(i + 2, stages.size());
                List<ExecutionStage> subList = stages.subList(i, end);
                partitions.add(WorkflowPartition.create(workflow.getWorkflowId(), i / 2, subList));
            }
        }
        return partitions;
    }

    private String extractRequiredCapability(WorkflowPartition partition) {
        if (partition.getStages() != null && !partition.getStages().isEmpty()) {
            ExecutionStage firstStage = partition.getStages().get(0);
            if (firstStage.getExecutionUnits() != null && !firstStage.getExecutionUnits().isEmpty()) {
                return firstStage.getExecutionUnits().get(0).getTargetCapability();
            }
        }
        return null;
    }
}
