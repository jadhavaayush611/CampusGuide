package com.campusguide.personal.ai.atlas.orchestration;

import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowInstance;
import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowRuntime;
import com.campusguide.personal.ai.atlas.orchestration.persistence.ResumeCoordinator;
import com.campusguide.personal.ai.atlas.orchestration.persistence.SuspensionManager;
import com.campusguide.personal.ai.atlas.orchestration.persistence.WorkflowLease;
import com.campusguide.personal.ai.atlas.orchestration.persistence.WorkflowPersistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LongRunningWorkflowTest {

    @Mock
    private WorkflowRuntime workflowRuntime;

    private WorkflowPersistence persistence;
    private SuspensionManager suspensionManager;
    private ResumeCoordinator resumeCoordinator;

    @BeforeEach
    void setUp() {
        persistence = new WorkflowPersistence();
        suspensionManager = new SuspensionManager(persistence, workflowRuntime);
        resumeCoordinator = new ResumeCoordinator(persistence, workflowRuntime);
    }

    @Test
    void testWorkflowLeaseExpiryAndRenewal() {
        WorkflowLease lease = WorkflowLease.grant("wf_long_1", "agent_1", 1000L);
        assertFalse(lease.isExpired());

        lease.renew(5000L);
        assertFalse(lease.isExpired());

        lease.release();
        assertTrue(lease.isExpired());
    }

    @Test
    void testSuspensionAndResumption() {
        WorkflowInstance instance = WorkflowInstance.builder()
                .instanceId("inst_long_100")
                .workflowId("wf_long_100")
                .build();

        when(workflowRuntime.getInstance("inst_long_100")).thenReturn(instance);

        boolean suspended = suspensionManager.suspendWorkflow("inst_long_100", "Awaiting external approval", "agent_1");
        assertTrue(suspended);

        Optional<WorkflowPersistence.WorkflowSnapshot> snapshot = persistence.getSnapshot("wf_long_100");
        assertTrue(snapshot.isPresent());

        Optional<WorkflowInstance> resumed = resumeCoordinator.resumeWorkflow("wf_long_100", "agent_1");
        assertTrue(resumed.isPresent());
    }
}
