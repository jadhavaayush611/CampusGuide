package com.campusguide.personal.ai.atlas.metrics;

import com.campusguide.personal.ai.atlas.exception.AtlasErrorCategory;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class AtlasMetrics {

    private final MeterRegistry registry;

    public AtlasMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordSuccess(String provider, String model) {
        Counter.builder("atlas.requests")
                .tag("status", "success")
                .tag("provider", provider != null ? provider : "unknown")
                .tag("model", model != null ? model : "unknown")
                .register(registry)
                .increment();
    }

    public void recordFailure(String provider, String model, AtlasErrorCategory errorCategory) {
        Counter.builder("atlas.requests")
                .tag("status", "failure")
                .tag("provider", provider != null ? provider : "unknown")
                .tag("model", model != null ? model : "unknown")
                .tag("error_category", errorCategory != null ? errorCategory.name() : "UNKNOWN")
                .register(registry)
                .increment();
    }

    public void recordRetry(String provider) {
        Counter.builder("atlas.requests.retries")
                .tag("provider", provider != null ? provider : "unknown")
                .register(registry)
                .increment();
    }

    public void recordTimeout(String provider) {
        Counter.builder("atlas.requests.timeout")
                .tag("provider", provider != null ? provider : "unknown")
                .register(registry)
                .increment();
    }

    public void recordCircuitBreakerEvent(String provider, String state) {
        Counter.builder("atlas.circuitbreaker.events")
                .tag("provider", provider != null ? provider : "unknown")
                .tag("state", state != null ? state : "UNKNOWN")
                .register(registry)
                .increment();
    }

    public void recordOrchestrationLatency(long durationMs) {
        Timer.builder("atlas.latency.orchestration")
                .register(registry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    public void recordContextAssemblyLatency(long durationMs) {
        Timer.builder("atlas.latency.context_assembly")
                .register(registry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    public void recordPromptAssemblyLatency(long durationMs) {
        Timer.builder("atlas.latency.prompt_assembly")
                .register(registry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    public void recordProviderLatency(long durationMs, String provider) {
        Timer.builder("atlas.latency.provider")
                .tag("provider", provider != null ? provider : "unknown")
                .register(registry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    public void recordTokens(int promptTokens, int completionTokens, int totalTokens, String provider, String model) {
        Counter.builder("atlas.tokens.prompt")
                .tag("provider", provider != null ? provider : "unknown")
                .tag("model", model != null ? model : "unknown")
                .register(registry)
                .increment(promptTokens);

        Counter.builder("atlas.tokens.completion")
                .tag("provider", provider != null ? provider : "unknown")
                .tag("model", model != null ? model : "unknown")
                .register(registry)
                .increment(completionTokens);

        Counter.builder("atlas.tokens.total")
                .tag("provider", provider != null ? provider : "unknown")
                .tag("model", model != null ? model : "unknown")
                .register(registry)
                .increment(totalTokens);
    }
}
