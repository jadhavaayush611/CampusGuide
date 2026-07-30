package com.campusguide.personal.ai.atlas.streaming;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Getter
@Slf4j
public class StreamSession {

    private final String sessionId;
    private final String conversationId;
    private final String userId;
    private final Instant createdAt;
    private final AtomicLong sequenceCounter = new AtomicLong(0);
    private final List<AtlasStreamEvent> eventBuffer = new CopyOnWriteArrayList<>();
    private final ConcurrentHashMap<String, StreamSubscription> subscriptions = new ConcurrentHashMap<>();
    private final AtomicBoolean active = new AtomicBoolean(true);
    private final int maxBufferSize;

    public StreamSession(String sessionId, String conversationId, String userId, int maxBufferSize) {
        this.sessionId = sessionId;
        this.conversationId = conversationId;
        this.userId = userId;
        this.createdAt = Instant.now();
        this.maxBufferSize = maxBufferSize > 0 ? maxBufferSize : 100;
    }

    public long nextSequence() {
        return sequenceCounter.incrementAndGet();
    }

    public synchronized void bufferEvent(AtlasStreamEvent event) {
        if (eventBuffer.size() >= maxBufferSize) {
            eventBuffer.remove(0);
        }
        eventBuffer.add(event);
    }

    public List<AtlasStreamEvent> getEventsAfterSequence(long sequence) {
        List<AtlasStreamEvent> result = new ArrayList<>();
        for (AtlasStreamEvent event : eventBuffer) {
            if (event.getSequence() > sequence) {
                result.add(event);
            }
        }
        return result;
    }

    public List<AtlasStreamEvent> getEventsAfterId(String lastEventId) {
        if (lastEventId == null || lastEventId.isBlank()) {
            return Collections.emptyList();
        }
        List<AtlasStreamEvent> result = new ArrayList<>();
        boolean found = false;
        for (AtlasStreamEvent event : eventBuffer) {
            if (found) {
                result.add(event);
            } else if (lastEventId.equals(event.getId())) {
                found = true;
            }
        }
        return result;
    }

    public void addSubscription(StreamSubscription subscription) {
        subscriptions.put(subscription.getSubscriptionId(), subscription);
    }

    public void removeSubscription(String subscriptionId) {
        subscriptions.remove(subscriptionId);
    }

    public void broadcast(AtlasStreamEvent event) {
        bufferEvent(event);
        subscriptions.values().forEach(sub -> {
            if (sub.isActive()) {
                try {
                    sub.emitEvent(event);
                } catch (Exception e) {
                    log.warn("Failed to send event to subscription {}: {}", sub.getSubscriptionId(), e.getMessage());
                }
            }
        });
    }

    public void close() {
        if (active.compareAndSet(true, false)) {
            subscriptions.values().forEach(sub -> {
                try {
                    sub.complete();
                } catch (Exception e) {
                    log.warn("Error completing subscription {}: {}", sub.getSubscriptionId(), e.getMessage());
                }
            });
            subscriptions.clear();
        }
    }
}
