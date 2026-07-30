package com.campusguide.personal.ai.atlas.streaming;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StreamPublisherTest {

    private StreamPublisher streamPublisher;
    private AtlasStreamingMetrics metrics;

    @BeforeEach
    void setUp() {
        metrics = new AtlasStreamingMetrics(null);
        streamPublisher = new StreamPublisher(metrics);
    }

    @Test
    void testCreateSession_Success() {
        StreamSession session = streamPublisher.createSession("conv-1", "user-1");
        assertNotNull(session);
        assertEquals("conv-1", session.getConversationId());
        assertEquals("user-1", session.getUserId());
        assertTrue(session.getActive().get());
        assertEquals(1, streamPublisher.getActiveStreamCount());
    }

    @Test
    void testPublishEvent_BuffersAndIncrementsSequence() {
        StreamSession session = streamPublisher.createSession("conv-1", "user-1");

        AtlasStreamEvent event1 = streamPublisher.publishEvent(session.getSessionId(), StreamEventType.CONNECTION_OPENED, Map.of("status", "connected"));
        assertNotNull(event1);
        assertEquals(1, event1.getSequence());
        assertEquals(StreamEventType.CONNECTION_OPENED, event1.getType());

        AtlasStreamEvent event2 = streamPublisher.publishEvent(session.getSessionId(), StreamEventType.THINKING, Map.of("step", "context_check"));
        assertNotNull(event2);
        assertEquals(2, event2.getSequence());

        List<AtlasStreamEvent> events = session.getEventsAfterSequence(0);
        assertEquals(2, events.size());
    }

    @Test
    void testSubscribeSse_AndReplayOnReconnect() {
        StreamSession session = streamPublisher.createSession("conv-1", "user-1");
        AtlasStreamEvent event1 = streamPublisher.publishEvent(session.getSessionId(), StreamEventType.CONNECTION_OPENED, Map.of("status", "connected"));

        SseEmitter emitter = streamPublisher.subscribeSse(session.getSessionId(), event1.getId());
        assertNotNull(emitter);

        assertEquals(1, metrics.getTotalReconnects());
    }

    @Test
    void testCloseSession() {
        StreamSession session = streamPublisher.createSession("conv-1", "user-1");
        assertEquals(1, streamPublisher.getActiveStreamCount());

        streamPublisher.closeSession(session.getSessionId());
        assertEquals(0, streamPublisher.getActiveStreamCount());
        assertFalse(session.getActive().get());
    }
}
