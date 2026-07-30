package com.campusguide.personal.ai.atlas.execution.runtime.events;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe event bus for publishing and subscribing to runtime events.
 */
@Slf4j
@Component
public class RuntimeEventBus implements EventPublisher {

    private final List<EventSubscriber> subscribers = new CopyOnWriteArrayList<>();

    public void subscribe(EventSubscriber subscriber) {
        if (subscriber != null && !subscribers.contains(subscriber)) {
            subscribers.add(subscriber);
        }
    }

    public void unsubscribe(EventSubscriber subscriber) {
        if (subscriber != null) {
            subscribers.remove(subscriber);
        }
    }

    @Override
    public void publishWorkflowEvent(WorkflowEvent event) {
        if (event == null) return;
        log.debug("Publishing WorkflowEvent: {} - {} (Instance: {})", event.getEventType(), event.getMessage(), event.getInstanceId());
        for (EventSubscriber subscriber : subscribers) {
            try {
                subscriber.onWorkflowEvent(event);
            } catch (Exception e) {
                log.error("Error dispatching WorkflowEvent to subscriber {}", subscriber.getClass().getName(), e);
            }
        }
    }

    @Override
    public void publishExecutionEvent(ExecutionEvent event) {
        if (event == null) return;
        log.debug("Publishing ExecutionEvent: {} (Unit: {}, Stage: {})", event.getEventType(), event.getUnitId(), event.getStageId());
        for (EventSubscriber subscriber : subscribers) {
            try {
                subscriber.onExecutionEvent(event);
            } catch (Exception e) {
                log.error("Error dispatching ExecutionEvent to subscriber {}", subscriber.getClass().getName(), e);
            }
        }
    }
}
