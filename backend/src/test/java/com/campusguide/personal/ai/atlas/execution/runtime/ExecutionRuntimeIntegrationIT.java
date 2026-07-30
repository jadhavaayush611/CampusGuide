package com.campusguide.personal.ai.atlas.execution.runtime;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionStage;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionUnit;
import com.campusguide.personal.ai.atlas.execution.model.StageCompletionPolicy;
import com.campusguide.personal.ai.atlas.execution.model.UnitPreparationStatus;
import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowInstance;
import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowRuntime;
import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ExecutionRuntimeIntegrationIT {

    @Autowired
    private WorkflowRuntime workflowRuntime;

    @Test
    void testEndToEndWorkflowExecutionWithMultipleStagesAndUnits() {
        ExecutionContext context = ExecutionContext.builder()
                .contextId("ctx_it_e2e")
                .userId("test_student")
                .build();

        // Stage 1: Parallel units
        Map<String, Object> p1 = new HashMap<>();
        p1.put("query", "math101");
        ExecutionUnit unit1 = ExecutionUnit.builder()
                .unitId("unit_academic_1")
                .targetCapability("academic.courses.lookup")
                .payload(p1)
                .status(UnitPreparationStatus.READY)
                .build();

        Map<String, Object> p2 = new HashMap<>();
        p2.put("building", "Science Hall");
        ExecutionUnit unit2 = ExecutionUnit.builder()
                .unitId("unit_campus_2")
                .targetCapability("campus.building.info")
                .payload(p2)
                .status(UnitPreparationStatus.READY)
                .build();

        ExecutionStage stage1 = ExecutionStage.builder()
                .stageId("stage_parallel_1")
                .stageName("Parallel Lookup Stage")
                .orderIndex(1)
                .parallel(true)
                .executionUnits(Arrays.asList(unit1, unit2))
                .completionPolicy(StageCompletionPolicy.ALL_MUST_SUCCEED)
                .build();

        // Stage 2: Sequential unit
        ExecutionUnit unit3 = ExecutionUnit.builder()
                .unitId("unit_calendar_3")
                .targetCapability("calendar.events.add")
                .status(UnitPreparationStatus.READY)
                .build();

        ExecutionStage stage2 = ExecutionStage.builder()
                .stageId("stage_sequential_2")
                .stageName("Sequential Calendar Stage")
                .orderIndex(2)
                .parallel(false)
                .executionUnits(Collections.singletonList(unit3))
                .completionPolicy(StageCompletionPolicy.ALL_MUST_SUCCEED)
                .build();

        ExecutableWorkflow workflow = ExecutableWorkflow.builder()
                .workflowId("wf_it_multistage")
                .stages(Arrays.asList(stage1, stage2))
                .build();

        WorkflowInstance instance = workflowRuntime.executeWorkflow(context, workflow);

        assertNotNull(instance);
        assertEquals(WorkflowState.COMPLETED, instance.getState());

        assertTrue(instance.getSession().isUnitCompleted("unit_academic_1"));
        assertTrue(instance.getSession().isUnitCompleted("unit_campus_2"));
        assertTrue(instance.getSession().isUnitCompleted("unit_calendar_3"));

        assertTrue(instance.getSession().isStageCompleted("stage_parallel_1"));
        assertTrue(instance.getSession().isStageCompleted("stage_sequential_2"));

        assertNotNull(instance.getEndTime());
        assertFalse(instance.getCheckpoints().isEmpty());
    }
}
