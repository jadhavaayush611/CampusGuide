package com.campusguide.personal.ai.atlas.execution.runtime.checkpoint;

import com.campusguide.personal.ai.atlas.execution.model.CheckpointType;
import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowInstance;
import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CheckpointManagerTest {

    private CheckpointManager checkpointManager;
    private WorkflowInstance instance;

    @BeforeEach
    void setUp() {
        checkpointManager = new CheckpointManager();
        instance = WorkflowInstance.builder()
                .instanceId("inst_chk_test")
                .workflowId("wf_chk_test")
                .state(WorkflowState.RUNNING)
                .currentStageIndex(1)
                .build();

        instance.getSession().setVariable("var1", "val1");
    }

    @Test
    void testCreateAndRetrieveCheckpoint() {
        RuntimeCheckpoint checkpoint = checkpointManager.createCheckpoint(instance, "stage_1", CheckpointType.PRE_STAGE, "Pre-stage test");

        assertNotNull(checkpoint);
        assertNotNull(checkpoint.getCheckpointId());
        assertEquals("wf_chk_test", checkpoint.getWorkflowId());

        Optional<RuntimeCheckpoint> latest = checkpointManager.getLatestCheckpoint("wf_chk_test");
        assertTrue(latest.isPresent());
        assertEquals(checkpoint.getCheckpointId(), latest.get().getCheckpointId());
    }

    @Test
    void testRestoreFromCheckpoint() {
        RuntimeCheckpoint checkpoint = checkpointManager.createCheckpoint(instance, "stage_1", CheckpointType.PRE_STAGE, "Pre-stage test");

        // Mutate session state
        instance.setCurrentStageIndex(5);
        instance.getSession().setVariable("var1", "mutated");

        boolean restored = checkpointManager.restoreFromCheckpoint(instance, checkpoint.getCheckpointId());
        assertTrue(restored);

        assertEquals(1, instance.getCurrentStageIndex());
        assertEquals("val1", instance.getSession().getVariable("var1"));
    }
}
