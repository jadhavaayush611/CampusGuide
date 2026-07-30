package com.campusguide.personal.ai.atlas.execution.runtime.workflow;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionStage;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionUnit;
import com.campusguide.personal.ai.atlas.execution.model.StageCompletionPolicy;
import com.campusguide.personal.ai.atlas.execution.model.UnitPreparationStatus;
import com.campusguide.personal.ai.atlas.execution.runtime.checkpoint.CheckpointManager;
import com.campusguide.personal.ai.atlas.execution.runtime.engine.ExecutionCoordinator;
import com.campusguide.personal.ai.atlas.execution.runtime.engine.ExecutionDispatcher;
import com.campusguide.personal.ai.atlas.execution.runtime.engine.ExecutionPipeline;
import com.campusguide.personal.ai.atlas.execution.runtime.events.RuntimeEventBus;
import com.campusguide.personal.ai.atlas.execution.runtime.human.ExecutionControlService;
import com.campusguide.personal.ai.atlas.execution.runtime.recovery.RecoveryCoordinator;
import com.campusguide.personal.ai.atlas.execution.runtime.retry.RetryEngine;
import com.campusguide.personal.ai.atlas.execution.runtime.rollback.CompensationExecutor;
import com.campusguide.personal.ai.atlas.execution.runtime.rollback.RollbackCoordinator;
import com.campusguide.personal.ai.atlas.execution.runtime.rollback.RollbackExecutor;
import com.campusguide.personal.ai.atlas.execution.runtime.security.ExecutionPermissionValidator;
import com.campusguide.personal.ai.atlas.execution.runtime.security.ExecutionSecurityManager;
import com.campusguide.personal.ai.atlas.execution.runtime.statemachine.ExecutionStateMachine;
import com.campusguide.personal.ai.atlas.execution.runtime.statemachine.TransitionValidator;
import com.campusguide.personal.ai.atlas.execution.runtime.tool.InternalServiceToolAdapter;
import com.campusguide.personal.ai.atlas.execution.runtime.tool.ToolExecutor;
import com.campusguide.personal.ai.atlas.execution.runtime.tool.ToolRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WorkflowRuntimeTest {

    private WorkflowRuntime workflowRuntime;

    @BeforeEach
    void setUp() {
        TransitionValidator transitionValidator = new TransitionValidator();
        ExecutionStateMachine stateMachine = new ExecutionStateMachine(transitionValidator);
        CheckpointManager checkpointManager = new CheckpointManager();
        RuntimeEventBus eventBus = new RuntimeEventBus();
        ExecutionControlService controlService = new ExecutionControlService();

        ToolRegistry toolRegistry = new ToolRegistry(List.of(new InternalServiceToolAdapter()));
        ExecutionSecurityManager securityManager = new ExecutionSecurityManager(new ExecutionPermissionValidator());
        RetryEngine retryEngine = new RetryEngine();
        ToolExecutor toolExecutor = new ToolExecutor(toolRegistry, securityManager, retryEngine);

        CompensationExecutor compensationExecutor = new CompensationExecutor(toolExecutor);
        RollbackCoordinator rollbackCoordinator = new RollbackCoordinator(compensationExecutor, stateMachine, checkpointManager, eventBus);
        RollbackExecutor rollbackExecutor = new RollbackExecutor(rollbackCoordinator);

        RecoveryCoordinator recoveryCoordinator = new RecoveryCoordinator(checkpointManager, rollbackExecutor, controlService);
        ExecutionDispatcher dispatcher = new ExecutionDispatcher(toolExecutor, eventBus);
        ExecutionPipeline pipeline = new ExecutionPipeline(dispatcher, checkpointManager);
        ExecutionCoordinator coordinator = new ExecutionCoordinator(pipeline, stateMachine, recoveryCoordinator);

        workflowRuntime = new WorkflowRuntime(coordinator, stateMachine, rollbackExecutor, controlService, eventBus);
    }

    @Test
    void testEndToEndWorkflowExecution() {
        ExecutionContext context = ExecutionContext.builder()
                .contextId("ctx_test_e2e")
                .userId("user_test")
                .build();

        ExecutionUnit unit1 = ExecutionUnit.builder()
                .unitId("unit_1")
                .targetCapability("internal.campus.lookup")
                .status(UnitPreparationStatus.READY)
                .mandatory(true)
                .build();

        ExecutionStage stage1 = ExecutionStage.builder()
                .stageId("stage_1")
                .stageName("Stage 1")
                .orderIndex(1)
                .executionUnits(Collections.singletonList(unit1))
                .parallel(false)
                .completionPolicy(StageCompletionPolicy.ALL_MUST_SUCCEED)
                .build();

        ExecutableWorkflow workflow = ExecutableWorkflow.builder()
                .workflowId("wf_e2e_1")
                .stages(Collections.singletonList(stage1))
                .build();

        WorkflowInstance instance = workflowRuntime.executeWorkflow(context, workflow);

        assertNotNull(instance);
        assertEquals(WorkflowState.COMPLETED, instance.getState());
        assertTrue(instance.getSession().isUnitCompleted("unit_1"));
        assertTrue(instance.getSession().isStageCompleted("stage_1"));
    }

    @Test
    void testPauseAndResumeWorkflow() {
        ExecutionContext context = ExecutionContext.builder()
                .contextId("ctx_pause_test")
                .build();

        ExecutableWorkflow workflow = ExecutableWorkflow.builder()
                .workflowId("wf_pause_1")
                .stages(Collections.emptyList())
                .build();

        WorkflowInstance instance = workflowRuntime.createInstance(context, workflow);
        workflowRuntime.pauseWorkflow(instance.getInstanceId(), "Manual pause requested");

        assertEquals(WorkflowState.PAUSED, instance.getState());

        workflowRuntime.resumeWorkflow(instance.getInstanceId());
        assertEquals(WorkflowState.COMPLETED, instance.getState());
    }

    @Test
    void testCancelWorkflow() {
        ExecutionContext context = ExecutionContext.builder()
                .contextId("ctx_cancel_test")
                .build();

        ExecutableWorkflow workflow = ExecutableWorkflow.builder()
                .workflowId("wf_cancel_1")
                .stages(Collections.emptyList())
                .build();

        WorkflowInstance instance = workflowRuntime.createInstance(context, workflow);
        workflowRuntime.cancelWorkflow(instance.getInstanceId(), "User requested cancellation");

        assertEquals(WorkflowState.CANCELLED, instance.getState());
    }
}
