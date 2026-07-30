package com.campusguide.personal.ai.atlas.orchestration;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionStage;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionUnit;
import com.campusguide.personal.ai.atlas.execution.runtime.engine.ExecutionRuntime;
import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowInstance;
import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowState;
import com.campusguide.personal.ai.atlas.orchestration.agent.AgentRuntime;
import com.campusguide.personal.ai.atlas.orchestration.delegation.DelegationEngine;
import com.campusguide.personal.ai.atlas.orchestration.delegation.TaskAssignment;
import com.campusguide.personal.ai.atlas.orchestration.workflow.DistributedWorkflowExecutor;
import com.campusguide.personal.ai.atlas.orchestration.workflow.PartitionStrategy;
import com.campusguide.personal.ai.atlas.orchestration.workflow.ResultMerger;
import com.campusguide.personal.ai.atlas.orchestration.workflow.WorkflowPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DistributedWorkflowExecutionTest {

    @Mock
    private ExecutionRuntime executionRuntime;
    @Mock
    private DelegationEngine delegationEngine;
    @Mock
    private AgentRuntime agentRuntime;

    private ResultMerger resultMerger;
    private DistributedWorkflowExecutor distributedExecutor;

    @BeforeEach
    void setUp() {
        resultMerger = new ResultMerger();
        distributedExecutor = new DistributedWorkflowExecutor(executionRuntime, delegationEngine, agentRuntime, resultMerger);
    }

    @Test
    void testPartitionWorkflowStageBased() {
        ExecutableWorkflow wf = ExecutableWorkflow.builder()
                .workflowId("wf_dist_1")
                .stages(List.of(
                        ExecutionStage.builder().stageId("stg_1").orderIndex(0).build(),
                        ExecutionStage.builder().stageId("stg_2").orderIndex(1).build()
                ))
                .build();

        List<WorkflowPartition> partitions = distributedExecutor.partitionWorkflow(wf, PartitionStrategy.STAGE_BASED);

        assertEquals(2, partitions.size());
        assertEquals(0, partitions.get(0).getOrderIndex());
        assertEquals(1, partitions.get(1).getOrderIndex());
    }

    @Test
    void testExecuteDistributedWorkflow() {
        ExecutableWorkflow wf = ExecutableWorkflow.builder()
                .workflowId("wf_dist_1")
                .stages(List.of(
                        ExecutionStage.builder().stageId("stg_1").executionUnits(List.of(
                                ExecutionUnit.builder().targetCapability("CourseQuery").build()
                        )).build()
                ))
                .build();

        ExecutionContext context = ExecutionContext.builder().contextId("ctx_dist").build();
        TaskAssignment assignment = TaskAssignment.create("p1", "agent_1", 5, null, "assigned");
        WorkflowInstance instance = WorkflowInstance.builder().workflowId("wf_dist_1").state(WorkflowState.COMPLETED).build();

        when(delegationEngine.delegateTask(anyString(), any(), anyInt(), any(), any()))
                .thenReturn(Optional.of(assignment));
        when(agentRuntime.executeDelegatedTask(anyString(), any(), any()))
                .thenReturn(instance);

        WorkflowInstance result = distributedExecutor.executeDistributed(context, wf, PartitionStrategy.STAGE_BASED);

        assertNotNull(result);
        assertEquals(WorkflowState.COMPLETED, result.getState());
    }
}
