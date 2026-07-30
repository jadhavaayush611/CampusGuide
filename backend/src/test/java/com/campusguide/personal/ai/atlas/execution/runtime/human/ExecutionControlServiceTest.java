package com.campusguide.personal.ai.atlas.execution.runtime.human;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ExecutionControlServiceTest {

    private ExecutionControlService controlService;

    @BeforeEach
    void setUp() {
        controlService = new ExecutionControlService();
    }

    @Test
    void testRegisterAndApproveWaitState() {
        ApprovalWaitState waitState = controlService.registerWaitState("wf_1", "inst_1", "unit_1", "Requires human approval", 60L);

        assertNotNull(waitState);
        assertEquals(ApprovalWaitState.Status.PENDING, waitState.getStatus());

        List<ApprovalWaitState> pending = controlService.getPendingApprovals();
        assertEquals(1, pending.size());

        boolean approved = controlService.submitApproval(waitState.getWaitId(), true, "admin_user", "Approved after review");
        assertTrue(approved);

        Optional<ApprovalWaitState> resolved = controlService.getWaitState(waitState.getWaitId());
        assertTrue(resolved.isPresent());
        assertEquals(ApprovalWaitState.Status.APPROVED, resolved.get().getStatus());
        assertEquals("admin_user", resolved.get().getResolvedBy());
    }

    @Test
    void testRecordIntervention() {
        controlService.recordIntervention("wf_2", "inst_2", "unit_2", "OVERRIDE", "operator_1", "OVERRIDDEN", "Forced override");

        List<ManualIntervention> history = controlService.getInterventionHistory();
        assertFalse(history.isEmpty());
        assertEquals("OVERRIDE", history.get(0).getInterventionType());
    }
}
