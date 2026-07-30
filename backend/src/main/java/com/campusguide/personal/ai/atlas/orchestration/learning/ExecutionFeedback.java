package com.campusguide.personal.ai.atlas.orchestration.learning;

import java.time.Instant;
import java.util.Map;

/**
 * Extension interface for execution feedback signal capture.
 * (No adaptive learning implementation).
 */
public interface ExecutionFeedback {

    String getWorkflowId();

    boolean isSuccess();

    long getExecutionTimeMs();

    Map<String, Object> getMetrics();

    Instant getTimestamp();
}
