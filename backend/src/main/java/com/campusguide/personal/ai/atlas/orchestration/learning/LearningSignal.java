package com.campusguide.personal.ai.atlas.orchestration.learning;

import java.time.Instant;
import java.util.Map;

/**
 * Extension interface for normalized learning signal extension hooks.
 * (No adaptive learning implementation).
 */
public interface LearningSignal {

    String getSignalId();

    String getSourceComponent();

    String getSignalType();

    Map<String, Object> getPayload();

    Instant getTimestamp();
}
