package com.campusguide.personal.ai.atlas.execution.runtime.events;

import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class RuntimeEventBusTest {

    private RuntimeEventBus eventBus;

    @BeforeEach
    void setUp() {
        eventBus = new RuntimeEventBus();
    }

    @Test
    void testWorkflowEventPublishAndSubscribe() {
        AtomicBoolean received = new AtomicBoolean(false);

        EventSubscriber subscriber = new EventSubscriber() {
            @Override
            public void onWorkflowEvent(WorkflowEvent event) {
                if ("WORKFLOW_STARTED".equals(event.getEventType())) {
                    received.set(true);
                }
            }
        };

        eventBus.subscribe(subscriber);

        WorkflowEvent event = WorkflowEvent.builder()
                .workflowId("wf_evt_1")
                .instanceId("inst_evt_1")
                .eventType("WORKFLOW_STARTED")
                .newState(WorkflowState.RUNNING)
                .build();

        eventBus.publishWorkflowEvent(event);

        assertTrue(received.get());
    }

    @Test
    void testExecutionEventPublishAndSubscribe() {
        AtomicBoolean received = new AtomicBoolean(false);

        EventSubscriber subscriber = new EventSubscriber() {
            @Override
            public void onExecutionEvent(ExecutionEvent event) {
                if ("UNIT_EXECUTED".equals(event.getEventType())) {
                    received.set(true);
                }
            }
        };

        eventBus.subscribe(subscriber);

        ExecutionEvent event = ExecutionEvent.builder()
                .workflowId("wf_evt_2")
                .unitId("unit_1")
                .eventType("UNIT_EXECUTED")
                .build();

        eventBus.publishExecutionEvent(event);

        assertTrue(received.get());
    }
}
