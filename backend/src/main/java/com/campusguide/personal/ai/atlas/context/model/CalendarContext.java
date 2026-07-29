package com.campusguide.personal.ai.atlas.context.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * Strongly-typed domain context model for Calendar.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarContext {

    private int todayEventsCount;
    private int upcomingEventsCount;

    @Builder.Default
    private List<EventSummary> todayEvents = Collections.emptyList();

    private String summary;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EventSummary {
        private String id;
        private String title;
        private String startTime;
        private String endTime;
        private String location;
    }
}
