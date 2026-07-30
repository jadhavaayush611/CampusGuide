package com.campusguide.personal.ai.atlas.orchestration.learning;

import java.time.Instant;
import java.util.Map;

/**
 * Extension interface for specialized agent performance feedback.
 * (No adaptive learning implementation).
 */
public interface AgentFeedback {

    String getAgentId();

    String getCapabilityId();

    double getTaskSuccessRate();

    Map<String, Object> getAttributes();

    Instant getTimestamp();
}
