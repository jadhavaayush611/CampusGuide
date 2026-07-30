package com.campusguide.personal.ai.atlas.streaming;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class AtlasStreamingMetrics {

    private final AtomicInteger activeStreams = new AtomicInteger(0);
    private final AtomicInteger totalReconnects = new AtomicInteger(0);
    private final AtomicInteger totalEventsEmitted = new AtomicInteger(0);
    private final AtomicInteger totalFailures = new AtomicInteger(0);
    private final ConcurrentHashMap<String, Long> sessionStartTimes = new ConcurrentHashMap<>();

    private final MeterRegistry meterRegistry;

    @Autowired
    public AtlasStreamingMetrics(@Autowired(required = false) MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        if (meterRegistry != null) {
            Gauge.builder("atlas.streaming.active_streams", activeStreams, AtomicInteger::get)
                    .description("Number of currently active streaming sessions")
                    .register(meterRegistry);
            Gauge.builder("atlas.streaming.reconnects.total", totalReconnects, AtomicInteger::get)
                    .description("Total number of SSE client reconnects")
                    .register(meterRegistry);
        }
    }

    public void recordStreamStart(String sessionId) {
        activeStreams.incrementAndGet();
        sessionStartTimes.put(sessionId, System.currentTimeMillis());
        log.debug("Streaming session started: sessionId={}", sessionId);
    }

    public void recordStreamEnd(String sessionId) {
        activeStreams.decrementAndGet();
        Long start = sessionStartTimes.remove(sessionId);
        if (start != null) {
            long durationMs = System.currentTimeMillis() - start;
            if (meterRegistry != null) {
                Timer.builder("atlas.streaming.duration")
                        .tag("status", "success")
                        .register(meterRegistry)
                        .record(durationMs, TimeUnit.MILLISECONDS);
            }
            log.debug("Streaming session ended: sessionId={}, durationMs={}", sessionId, durationMs);
        }
    }

    public void recordReconnect(String sessionId, String lastEventId) {
        totalReconnects.incrementAndGet();
        if (meterRegistry != null) {
            Counter.builder("atlas.streaming.reconnects").register(meterRegistry).increment();
        }
        log.info("SSE reconnect requested for session={} with lastEventId={}", sessionId, lastEventId);
    }

    public void recordEventEmitted(StreamEventType type) {
        totalEventsEmitted.incrementAndGet();
        if (meterRegistry != null) {
            Counter.builder("atlas.streaming.events")
                    .tag("type", type.name())
                    .register(meterRegistry)
                    .increment();
        }
    }

    public void recordFailure(String sessionId, String errorType) {
        totalFailures.incrementAndGet();
        if (meterRegistry != null) {
            Counter.builder("atlas.streaming.failures")
                    .tag("errorType", errorType != null ? errorType : "unknown")
                    .register(meterRegistry)
                    .increment();
        }
        log.warn("Streaming failure recorded for session={}: errorType={}", sessionId, errorType);
    }

    public int getActiveStreamCount() {
        return activeStreams.get();
    }

    public int getTotalReconnects() {
        return totalReconnects.get();
    }

    public int getTotalEventsEmitted() {
        return totalEventsEmitted.get();
    }

    public int getTotalFailures() {
        return totalFailures.get();
    }

    public Map<String, Object> getMetricsSummary() {
        return Map.of(
                "activeStreams", activeStreams.get(),
                "totalReconnects", totalReconnects.get(),
                "totalEventsEmitted", totalEventsEmitted.get(),
                "totalFailures", totalFailures.get()
        );
    }
}
