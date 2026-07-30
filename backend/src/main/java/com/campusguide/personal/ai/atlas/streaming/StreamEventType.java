package com.campusguide.personal.ai.atlas.streaming;

public enum StreamEventType {
    CONNECTION_OPENED,
    THINKING,
    REASONING,
    PLANNING,
    EXECUTION_STARTED,
    TOOL_STARTED,
    TOOL_COMPLETED,
    EXECUTION_COMPLETED,
    RESPONSE_TOKEN,
    COMPLETION,
    ERROR,
    CONNECTION_CLOSED
}
