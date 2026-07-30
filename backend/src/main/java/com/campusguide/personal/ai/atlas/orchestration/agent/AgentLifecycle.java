package com.campusguide.personal.ai.atlas.orchestration.agent;

/**
 * Lifecycle states for specialized Atlas agents.
 */
public enum AgentLifecycle {
    UNINITIALIZED,
    INITIALIZING,
    READY,
    RUNNING,
    PAUSED,
    STOPPED,
    TERMINATED,
    FAILED;

    public boolean isTerminal() {
        return this == TERMINATED || this == FAILED || this == STOPPED;
    }

    public boolean isAvailable() {
        return this == READY || this == RUNNING;
    }
}
