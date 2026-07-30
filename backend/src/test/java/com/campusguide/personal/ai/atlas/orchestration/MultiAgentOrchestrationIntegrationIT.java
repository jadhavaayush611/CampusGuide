package com.campusguide.personal.ai.atlas.orchestration;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionStage;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionUnit;
import com.campusguide.personal.ai.atlas.execution.runtime.engine.ExecutionCoordinator;
import com.campusguide.personal.ai.atlas.execution.runtime.events.EventPublisher;
import com.campusguide.personal.ai.atlas.execution.runtime.human.ExecutionControlService;
import com.campusguide.personal.ai.atlas.execution.runtime.rollback.RollbackExecutor;
import com.campusguide.personal.ai.atlas.execution.runtime.statemachine.ExecutionStateMachine;
import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowInstance;
import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowRuntime;
import com.campusguide.personal.ai.atlas.orchestration.agent.AgentRuntime;
import com.campusguide.personal.ai.atlas.orchestration.agent.AtlasAgent;
import com.campusguide.personal.ai.atlas.orchestration.communication.AgentMessage;
import com.campusguide.personal.ai.atlas.orchestration.communication.CommunicationBus;
import com.campusguide.personal.ai.atlas.orchestration.communication.ConversationContext;
import com.campusguide.personal.ai.atlas.orchestration.coordination.CoordinationEngine;
import com.campusguide.personal.ai.atlas.orchestration.coordination.SynchronizationBarrier;
import com.campusguide.personal.ai.atlas.orchestration.delegation.AssignmentStrategy;
import com.campusguide.personal.ai.atlas.orchestration.delegation.DelegationEngine;
import com.campusguide.personal.ai.atlas.orchestration.delegation.DelegationPolicy;
import com.campusguide.personal.ai.atlas.orchestration.delegation.TaskAssignment;
import com.campusguide.personal.ai.atlas.orchestration.explainability.OrchestrationExplanationEngine;
import com.campusguide.personal.ai.atlas.orchestration.memory.MemoryCoordinator;
import com.campusguide.personal.ai.atlas.orchestration.memory.MemoryLease;
import com.campusguide.personal.ai.atlas.orchestration.memory.SharedMemory;
import com.campusguide.personal.ai.atlas.orchestration.metrics.OrchestrationMetrics;
import com.campusguide.personal.ai.atlas.orchestration.persistence.ResumeCoordinator;
import com.campusguide.personal.ai.atlas.orchestration.persistence.SuspensionManager;
import com.campusguide.personal.ai.atlas.orchestration.persistence.WorkflowPersistence;
import com.campusguide.personal.ai.atlas.orchestration.registry.AgentCapability;
import com.campusguide.personal.ai.atlas.orchestration.registry.AgentDescriptor;
import com.campusguide.personal.ai.atlas.orchestration.registry.AgentMetadata;
import com.campusguide.personal.ai.atlas.orchestration.registry.AgentRegistry;
import com.campusguide.personal.ai.atlas.orchestration.replanning.DynamicReplanner;
import com.campusguide.personal.ai.atlas.orchestration.replanning.ReplanningDecision;
import com.campusguide.personal.ai.atlas.orchestration.replanning.ReplanningTrigger;
import com.campusguide.personal.ai.atlas.orchestration.supervisor.ExecutionAuditor;
import com.campusguide.personal.ai.atlas.orchestration.supervisor.PolicySupervisor;
import com.campusguide.personal.ai.atlas.orchestration.supervisor.RecoveryCoordinator;
import com.campusguide.personal.ai.atlas.orchestration.supervisor.SupervisorAgent;
import com.campusguide.personal.ai.atlas.orchestration.workflow.DistributedWorkflowExecutor;
import com.campusguide.personal.ai.atlas.orchestration.workflow.PartitionStrategy;
import com.campusguide.personal.ai.atlas.orchestration.workflow.ResultMerger;
import com.campusguide.personal.ai.atlas.planning.engine.PlanningEngine;
import com.campusguide.personal.ai.atlas.execution.engine.ExecutionPreparationEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MultiAgentOrchestrationIntegrationIT {

    @Mock
    private ExecutionCoordinator executionCoordinator;
    @Mock
    private ExecutionStateMachine stateMachine;
    @Mock
    private RollbackExecutor rollbackExecutor;
    @Mock
    private ExecutionControlService controlService;
    @Mock
    private EventPublisher eventPublisher;
    @Mock
    private PlanningEngine planningEngine;
    @Mock
    private ExecutionPreparationEngine preparationEngine;

    private WorkflowRuntime workflowRuntime;
    private AgentRegistry agentRegistry;
    private AgentRuntime agentRuntime;
    private DelegationEngine delegationEngine;
    private CommunicationBus communicationBus;
    private SharedMemory sharedMemory;
    private MemoryCoordinator memoryCoordinator;
    private CoordinationEngine coordinationEngine;
    private DistributedWorkflowExecutor distributedWorkflowExecutor;
    private DynamicReplanner dynamicReplanner;
    private SupervisorAgent supervisorAgent;
    private ExecutionAuditor executionAuditor;
    private RecoveryCoordinator supervisorRecoveryCoordinator;
    private PolicySupervisor policySupervisor;
    private WorkflowPersistence workflowPersistence;
    private SuspensionManager suspensionManager;
    private ResumeCoordinator resumeCoordinator;
    private OrchestrationExplanationEngine explanationEngine;
    private OrchestrationMetrics metrics;

    @BeforeEach
    void setUp() {
        workflowRuntime = new WorkflowRuntime(executionCoordinator, stateMachine, rollbackExecutor, controlService, eventPublisher);
        agentRegistry = new AgentRegistry();
        agentRuntime = new AgentRuntime(workflowRuntime);
        delegationEngine = new DelegationEngine(agentRegistry);
        communicationBus = new CommunicationBus();
        sharedMemory = new SharedMemory();
        memoryCoordinator = new MemoryCoordinator(sharedMemory);
        coordinationEngine = new CoordinationEngine();
        distributedWorkflowExecutor = new DistributedWorkflowExecutor(workflowRuntime, delegationEngine, agentRuntime, new ResultMerger());
        dynamicReplanner = new DynamicReplanner(planningEngine, preparationEngine);
        supervisorAgent = new SupervisorAgent();
        executionAuditor = new ExecutionAuditor(agentRegistry, agentRuntime);
        supervisorRecoveryCoordinator = new RecoveryCoordinator(agentRegistry, agentRuntime);
        policySupervisor = new PolicySupervisor(agentRegistry);
        workflowPersistence = new WorkflowPersistence();
        suspensionManager = new SuspensionManager(workflowPersistence, workflowRuntime);
        resumeCoordinator = new ResumeCoordinator(workflowPersistence, workflowRuntime);
        explanationEngine = new OrchestrationExplanationEngine();
        metrics = new OrchestrationMetrics(null);
    }

    @Test
    void testEndToEndMultiAgentOrchestration() {
        // 1. Register specialized agents
        AgentDescriptor academicAgent = AgentDescriptor.builder()
                .agentId("agent_academic_1")
                .name("Academic Agent")
                .agentType("ACADEMIC")
                .healthStatus(AgentDescriptor.HealthStatus.HEALTHY)
                .maxCapacity(10)
                .currentLoad(0)
                .capabilities(List.of(AgentCapability.of("AcademicQuery", "academic")))
                .metadata(AgentMetadata.defaultFor("Academic", "academic"))
                .build();

        AgentDescriptor plannerAgent = AgentDescriptor.builder()
                .agentId("agent_planner_1")
                .name("Planner Agent")
                .agentType("PLANNER")
                .healthStatus(AgentDescriptor.HealthStatus.HEALTHY)
                .maxCapacity(10)
                .currentLoad(0)
                .capabilities(List.of(AgentCapability.of("TaskScheduler", "planner")))
                .metadata(AgentMetadata.defaultFor("Planner", "planner"))
                .build();

        agentRegistry.registerAgent(academicAgent);
        agentRegistry.registerAgent(plannerAgent);

        AtlasAgent aAgent = agentRuntime.registerAgent(academicAgent);
        AtlasAgent pAgent = agentRuntime.registerAgent(plannerAgent);

        assertNotNull(aAgent);
        assertNotNull(pAgent);

        // 2. Inter-agent communication
        ConversationContext conv = communicationBus.createConversation("agent_academic_1", List.of("agent_planner_1"));
        communicationBus.sendMessage(AgentMessage.request("agent_academic_1", "agent_planner_1", conv.getConversationId(), Map.of("query", "exam schedule")));
        List<AgentMessage> inbox = communicationBus.fetchMessages("agent_planner_1");
        assertEquals(1, inbox.size());

        // 3. Shared memory & leasing
        Optional<MemoryLease> leaseOpt = memoryCoordinator.acquireLease("shared_ns", "exam_data", "agent_academic_1", 10000L);
        assertTrue(leaseOpt.isPresent());
        memoryCoordinator.updateStateWithLease(leaseOpt.get(), "Calculus Exam @ 10 AM");
        assertEquals("Calculus Exam @ 10 AM", sharedMemory.get("shared_ns", "exam_data").get().getValue());

        // 4. Coordination & Barriers
        SynchronizationBarrier barrier = coordinationEngine.createBarrier(2);
        coordinationEngine.arriveAndAwait(barrier.getBarrierId(), "agent_academic_1", Map.of("academic_output", "done"), 1000L);
        coordinationEngine.arriveAndAwait(barrier.getBarrierId(), "agent_planner_1", Map.of("planner_output", "done"), 1000L);
        assertTrue(barrier.isReleased());

        // 5. Delegation
        Optional<TaskAssignment> assignmentOpt = delegationEngine.delegateTask(
                "task_multi_1", "AcademicQuery", 5, "academic", DelegationPolicy.defaultPolicy());
        assertTrue(assignmentOpt.isPresent());
        assertEquals("agent_academic_1", assignmentOpt.get().getAgentId());

        // 6. Distributed workflow execution over Execution Runtime
        when(executionCoordinator.executeWorkflow(any())).thenReturn(true);

        ExecutableWorkflow workflow = ExecutableWorkflow.builder()
                .workflowId("wf_e2e_1")
                .stages(List.of(
                        ExecutionStage.builder().stageId("stg_1").executionUnits(List.of(
                                ExecutionUnit.builder().targetCapability("AcademicQuery").build()
                        )).build()
                ))
                .build();

        ExecutionContext context = ExecutionContext.builder().contextId("ctx_e2e_1").build();
        WorkflowInstance instance = distributedWorkflowExecutor.executeDistributed(context, workflow, PartitionStrategy.STAGE_BASED);

        assertNotNull(instance);

        // 7. Supervision & auditing
        ExecutionAuditor.AuditReport audit = executionAuditor.auditExecution(5000);
        assertEquals(2, audit.getTotalAgentsAudited());

        // 8. Long-running workflow suspension & resumption
        suspensionManager.suspendWorkflow(instance.getInstanceId(), "Long pause requirement", "agent_academic_1");
        Optional<WorkflowInstance> resumed = resumeCoordinator.resumeWorkflow(instance.getWorkflowId(), "agent_academic_1");
        assertTrue(resumed.isPresent());

        // 9. Explainability
        String explanation = explanationEngine.explainSupervisorIntervention("agent_academic_1", "HEALTH_CHECK", "Routine check");
        assertTrue(explanation.contains("agent_academic_1"));
    }
}
