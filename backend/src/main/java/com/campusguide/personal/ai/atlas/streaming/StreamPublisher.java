package com.campusguide.personal.ai.atlas.streaming;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Service
@RequiredArgsConstructor
@Slf4j
public class StreamPublisher {

    private final ConcurrentHashMap<String, StreamSession> activeSessions = new ConcurrentHashMap<>();
    private final AtlasStreamingMetrics metrics;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();

    private static final long SSE_TIMEOUT_MS = 300_000L; // 5 minutes
    private static final int MAX_BUFFER_SIZE = 200;

    public StreamSession createSession(String conversationId, String userId) {
        String sessionId = "stream_" + UUID.randomUUID().toString();
        StreamSession session = new StreamSession(sessionId, conversationId, userId, MAX_BUFFER_SIZE);
        activeSessions.put(sessionId, session);
        metrics.recordStreamStart(sessionId);
        log.info("Created stream session: sessionId={}, conversationId={}, userId={}", sessionId, conversationId, userId);
        return session;
    }

    public StreamSession getSession(String sessionId) {
        return activeSessions.get(sessionId);
    }

    public SseEmitter subscribeSse(String sessionId, String lastEventId) {
        StreamSession session = activeSessions.get(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Stream session not found: " + sessionId);
        }

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        String subscriptionId = "sub_" + UUID.randomUUID().toString();

        StreamSubscription subscription = new StreamSubscription() {
            private boolean active = true;

            @Override
            public String getSubscriptionId() {
                return subscriptionId;
            }

            @Override
            public String getSessionId() {
                return sessionId;
            }

            @Override
            public String getLastEventId() {
                return lastEventId;
            }

            @Override
            public boolean isActive() {
                return active;
            }

            @Override
            public void emitEvent(AtlasStreamEvent event) {
                if (!active) return;
                try {
                    emitter.send(SseEmitter.event()
                            .id(event.getId())
                            .name(event.getType().name())
                            .data(event));
                    metrics.recordEventEmitted(event.getType());
                } catch (IOException e) {
                    active = false;
                    log.warn("Error emitting SSE event {} to subscription {}: {}", event.getType(), subscriptionId, e.getMessage());
                    emitter.completeWithError(e);
                }
            }

            @Override
            public void emitError(Throwable error) {
                if (!active) return;
                try {
                    AtlasStreamEvent errEvent = AtlasStreamEvent.builder()
                            .id(UUID.randomUUID().toString())
                            .type(StreamEventType.ERROR)
                            .conversationId(session.getConversationId())
                            .payload(Map.of("error", error.getMessage() != null ? error.getMessage() : "Stream error"))
                            .sequence(session.nextSequence())
                            .timestamp(Instant.now())
                            .build();
                    emitter.send(SseEmitter.event()
                            .id(errEvent.getId())
                            .name(StreamEventType.ERROR.name())
                            .data(errEvent));
                } catch (IOException e) {
                    log.warn("Failed to send error event over SSE: {}", e.getMessage());
                } finally {
                    active = false;
                    emitter.completeWithError(error);
                }
            }

            @Override
            public void complete() {
                if (!active) return;
                active = false;
                try {
                    emitter.complete();
                } catch (Exception e) {
                    log.debug("Exception on emitter complete: {}", e.getMessage());
                }
            }

            @Override
            public void cancel() {
                active = false;
                session.removeSubscription(subscriptionId);
            }
        };

        session.addSubscription(subscription);

        emitter.onCompletion(() -> {
            subscription.cancel();
            log.debug("SSE emitter completed for subscription {}", subscriptionId);
        });

        emitter.onTimeout(() -> {
            subscription.cancel();
            metrics.recordFailure(sessionId, "SSE_TIMEOUT");
            log.warn("SSE emitter timed out for subscription {}", subscriptionId);
        });

        emitter.onError((ex) -> {
            subscription.cancel();
            metrics.recordFailure(sessionId, ex.getClass().getSimpleName());
            log.warn("SSE emitter error for subscription {}: {}", subscriptionId, ex.getMessage());
        });

        // Replay missed events if client reconnected with Last-Event-ID
        if (lastEventId != null && !lastEventId.isBlank()) {
            metrics.recordReconnect(sessionId, lastEventId);
            List<AtlasStreamEvent> missedEvents = session.getEventsAfterId(lastEventId);
            for (AtlasStreamEvent missed : missedEvents) {
                subscription.emitEvent(missed);
            }
        }

        return emitter;
    }

    public AtlasStreamEvent publishEvent(String sessionId, StreamEventType type, Object payload) {
        StreamSession session = activeSessions.get(sessionId);
        if (session == null) {
            log.warn("Cannot publish event to non-existent session: {}", sessionId);
            return null;
        }
        AtlasStreamEvent event = AtlasStreamEvent.builder()
                .id(UUID.randomUUID().toString())
                .type(type)
                .conversationId(session.getConversationId())
                .payload(payload)
                .timestamp(Instant.now())
                .sequence(session.nextSequence())
                .build();

        session.broadcast(event);

        if (type == StreamEventType.CONNECTION_CLOSED || type == StreamEventType.COMPLETION) {
            if (type == StreamEventType.CONNECTION_CLOSED) {
                closeSession(sessionId);
            }
        }
        return event;
    }

    public void closeSession(String sessionId) {
        StreamSession session = activeSessions.remove(sessionId);
        if (session != null) {
            session.close();
            metrics.recordStreamEnd(sessionId);
            log.info("Closed stream session: {}", sessionId);
        }
    }

    public int getActiveStreamCount() {
        return activeSessions.size();
    }
}
