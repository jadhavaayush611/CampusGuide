package com.campusguide.personal.ai.atlas.execution.runtime.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Micrometer operational metrics provider for Execution Runtime Engine.
 */
@Component
public class RuntimeMetrics {

    private final MeterRegistry registry;

    @Autowired
    public RuntimeMetrics(@Autowired(required = false) MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordWorkflowExecution(String status, long durationMs) {
        if (registry != null) {
            Counter.builder("atlas.runtime.workflow.executions")
                    .tag("status", status != null ? status : "UNKNOWN")
                    .register(registry)
                    .increment();

            Timer.builder("atlas.runtime.workflow.duration")
                    .tag("status", status != null ? status : "UNKNOWN")
                    .register(registry)
                    .record(Duration.ofMillis(durationMs));
        }
    }

    public void recordUnitExecution(String capability, String status, long durationMs) {
        if (registry != null) {
            Counter.builder("atlas.runtime.unit.executions")
                    .tag("capability", capability != null ? capability : "UNKNOWN")
                    .tag("status", status != null ? status : "UNKNOWN")
                    .register(registry)
                    .increment();

            Timer.builder("atlas.runtime.unit.duration")
                    .tag("capability", capability != null ? capability : "UNKNOWN")
                    .register(registry)
                    .record(Duration.ofMillis(durationMs));
        }
    }

    public void recordRetry(String unitId) {
        if (registry != null) {
            Counter.builder("atlas.runtime.retries")
                    .register(registry)
                    .increment();
        }
    }

    public void recordRollback(String workflowId) {
        if (registry != null) {
            Counter.builder("atlas.runtime.rollbacks")
                    .register(registry)
                    .increment();
        }
    }
}
