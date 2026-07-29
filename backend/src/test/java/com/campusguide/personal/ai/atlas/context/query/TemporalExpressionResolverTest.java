package com.campusguide.personal.ai.atlas.context.query;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TemporalExpressionResolverTest {

    private TemporalExpressionResolver resolver;
    private LocalDateTime referenceTime;

    @BeforeEach
    void setUp() {
        resolver = new TemporalExpressionResolver();
        referenceTime = LocalDateTime.of(2026, 7, 29, 10, 0); // Wednesday
    }

    @Test
    @DisplayName("Should resolve 'tomorrow' relative to reference time")
    void testResolve_Tomorrow() {
        TemporalInformation info = resolver.resolve("What classes do I have tomorrow?", referenceTime);

        assertTrue(info.isResolved());
        assertEquals("tomorrow", info.getRawExpression());
        assertEquals("DAY", info.getResolutionType());
        assertEquals(2026, info.getStartTime().getYear());
        assertEquals(7, info.getStartTime().getMonthValue());
        assertEquals(30, info.getStartTime().getDayOfMonth());
    }

    @Test
    @DisplayName("Should resolve 'next week' relative to reference time")
    void testResolve_NextWeek() {
        TemporalInformation info = resolver.resolve("Show me my schedule for next week", referenceTime);

        assertTrue(info.isResolved());
        assertEquals("next week", info.getRawExpression());
        assertEquals("WEEK", info.getResolutionType());
        // Reference is Wednesday 2026-07-29. Next Monday is 2026-08-03
        assertEquals(2026, info.getStartTime().getYear());
        assertEquals(8, info.getStartTime().getMonthValue());
        assertEquals(3, info.getStartTime().getDayOfMonth());
    }

    @Test
    @DisplayName("Should resolve 'after lunch' into afternoon temporal range")
    void testResolve_AfterLunch() {
        TemporalInformation info = resolver.resolve("What events are happening after lunch?", referenceTime);

        assertTrue(info.isResolved());
        assertEquals("after lunch", info.getRawExpression());
        assertEquals("TIME_OF_DAY", info.getResolutionType());
        assertEquals(13, info.getStartTime().getHour());
        assertEquals(17, info.getEndTime().getHour());
    }

    @Test
    @DisplayName("Should return unresolved TemporalInformation for queries without temporal cues")
    void testResolve_NonTemporal() {
        TemporalInformation info = resolver.resolve("Where is the science building?", referenceTime);
        assertFalse(info.isResolved());
    }
}
