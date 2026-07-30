package com.campusguide.personal.ai.atlas.orchestration.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Micrometer operational metrics provider for Multi-Agent Orchestration Engine.
 * Captures latency, utilization, delegation, synchronization, replanning, and supervisor interventions
 * without exposing sensitive execution data.
 */
@Component
public class OrchestrationMetrics {

    private final MeterRegistry registry;
    private final AtomicInteger activeAgentCount = new AtomicInteger(0);

    @Autowired
    public OrchestrationMetrics(@Autowired(required = false) MeterRegistry registry) {
        this.registry = registry;
        if (this.registry != null) {
            Gauge.builder("atlas.orchestration.agent.utilization", activeAgentCount, AtomicInteger::get)
                    .description("Current number of active agents handling load")
                    .register(this.registry);
        }
    }

    public void recordOrchestrationLatency(String operation, long durationMs) {
        if (registry != null) {
            Timer.builder("atlas.orchestration.latency")
                    .tag("operation", operation != null ? operation : "UNKNOWN")
                    .register(registry)
                    .record(Duration.ofMillis(durationMs));
        }
    }

    public void recordDelegation(String strategy, String status) {
        if (registry != null) {
            Counter.builder("atlas.orchestration.delegation.count")
                    .tag("strategy", strategy != null ? strategy : "UNKNOWN")
                    .tag("status", status != null ? status : "UNKNOWN")
                    .register(registry)
                    .increment();
        }
    }

    public void recordSynchronizationDelay(long delayMs) {
        if (registry != null) {
            Timer.builder("atlas.orchestration.synchronization.delay")
                    .register(registry)
                    .record(Duration.ofMillis(delayMs));
        }
    }

    public void recordDistributedExecution(String status) {
        if (registry != null) {
            Counter.builder("atlas.orchestration.distributed.executions")
                    .tag("status", status != null ? status : "UNKNOWN")
                    .register(registry)
                    .increment();
        }
    }

    public void recordReplanning(String trigger) {
        if (registry != null) {
            Counter.builder("atlas.orchestration.replanning.frequency")
                    .tag("trigger", trigger != null ? trigger : "UNKNOWN")
                    .register(registry)
                    .increment();
        }
    }

    public void recordSupervisorIntervention(String action) {
        if (registry != null) {
            Counter.builder("atlas.orchestration.supervisor.interventions")
                    .tag("action", action != null ? action : "UNKNOWN")
                    .register(registry)
                    .increment();
        }
    }

    public void updateActiveAgentCount(int count) {
        activeAgentCount.set(count);
    }
}
