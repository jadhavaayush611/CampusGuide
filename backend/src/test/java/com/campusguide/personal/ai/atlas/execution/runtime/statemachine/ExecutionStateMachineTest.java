package com.campusguide.personal.ai.atlas.execution.runtime.statemachine;

import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowInstance;
import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExecutionStateMachineTest {

    private TransitionValidator transitionValidator;
    private ExecutionStateMachine stateMachine;
    private WorkflowInstance instance;

    @BeforeEach
    void setUp() {
        transitionValidator = new TransitionValidator();
        stateMachine = new ExecutionStateMachine(transitionValidator);
        instance = WorkflowInstance.builder()
                .instanceId("wfinst_test")
                .workflowId("wf_test")
                .state(WorkflowState.CREATED)
                .build();
    }

    @Test
    void testValidTransitions() {
        assertTrue(stateMachine.transition(instance, WorkflowState.VALIDATED, "Validation success"));
        assertEquals(WorkflowState.VALIDATED, instance.getState());

        assertTrue(stateMachine.transition(instance, WorkflowState.READY, "Ready for run"));
        assertEquals(WorkflowState.READY, instance.getState());

        assertTrue(stateMachine.transition(instance, WorkflowState.RUNNING, "Running stage"));
        assertEquals(WorkflowState.RUNNING, instance.getState());

        assertTrue(stateMachine.transition(instance, WorkflowState.COMPLETED, "Execution complete"));
        assertEquals(WorkflowState.COMPLETED, instance.getState());
    }

    @Test
    void testInvalidTransitionThrowsException() {
        assertThrows(IllegalStateException.class, () ->
                stateMachine.transition(instance, WorkflowState.COMPLETED, "Illegal transition"));
    }

    @Test
    void testSelfTransitionAllowed() {
        assertTrue(stateMachine.transition(instance, WorkflowState.CREATED, "Self transition"));
        assertEquals(WorkflowState.CREATED, instance.getState());
    }

    @Test
    void testRecoveryStatesTransitions() {
        stateMachine.transition(instance, WorkflowState.VALIDATED, "valid");
        stateMachine.transition(instance, WorkflowState.READY, "ready");
        stateMachine.transition(instance, WorkflowState.RUNNING, "running");

        assertTrue(stateMachine.transition(instance, WorkflowState.FAILED, "error"));
        assertEquals(WorkflowState.FAILED, instance.getState());

        assertTrue(stateMachine.transition(instance, WorkflowState.RETRYING, "retry"));
        assertEquals(WorkflowState.RETRYING, instance.getState());
    }
}
