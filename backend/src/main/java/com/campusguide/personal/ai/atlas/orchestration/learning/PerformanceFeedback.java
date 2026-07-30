package com.campusguide.personal.ai.atlas.orchestration.learning;

import java.time.Instant;

/**
 * Extension interface for system resource and timing feedback.
 * (No adaptive learning implementation).
 */
public interface PerformanceFeedback {

    long getLatencyMs();

    double getCpuLoad();

    long getMemoryUsedBytes();

    Instant getTimestamp();
}
