package com.campusguide.personal.ai.atlas.context.service;

import com.campusguide.personal.ai.atlas.context.model.CalendarContext;
import com.campusguide.personal.ai.atlas.context.model.CalendarContext.EventSummary;
import com.campusguide.personal.ai.atlas.dto.AtlasChatRequest;
import com.campusguide.personal.calendar.entity.CalendarEntry;
import com.campusguide.personal.calendar.repository.CalendarEntryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service responsible for querying, summarizing, and normalizing Calendar domain context.
 */
@Service
@Slf4j
public class CalendarContextService {

    private static final int MAX_TODAY_EVENTS = 5;
    private final CalendarEntryRepository calendarEntryRepository;

    public CalendarContextService(@Autowired(required = false) CalendarEntryRepository calendarEntryRepository) {
        this.calendarEntryRepository = calendarEntryRepository;
    }

    /**
     * Queries, filters, and normalizes calendar context with deterministic ordering and bounded limits.
     *
     * @param userId target user ID
     * @param request chat request
     * @return normalized CalendarContext
     */
    public CalendarContext getCalendarContext(String userId, AtlasChatRequest request) {
        int todayEventsCount = 0;
        int upcomingEventsCount = 0;
        List<EventSummary> todayEvents = new ArrayList<>();

        if (calendarEntryRepository != null && StringUtils.hasText(userId)) {
            try {
                List<CalendarEntry> userEntries = calendarEntryRepository.findByUserIdOrderByStartTimeAscEndTimeAsc(userId);
                if (userEntries != null && !userEntries.isEmpty()) {
                    LocalDate today = LocalDate.now();

                    for (CalendarEntry entry : userEntries) {
                        if (entry.getStartTime() != null) {
                            LocalDate eventDate = entry.getStartTime().toLocalDate();
                            if (eventDate.isEqual(today)) {
                                todayEventsCount++;
                            } else if (eventDate.isAfter(today)) {
                                upcomingEventsCount++;
                            }
                        }
                    }

                    // Enforce deterministic ordering: today's events sorted by start time asc, title asc
                    todayEvents = userEntries.stream()
                            .filter(e -> e.getStartTime() != null && e.getStartTime().toLocalDate().isEqual(today))
                            .sorted(Comparator
                                    .comparing((CalendarEntry e) -> e.getStartTime() != null ? e.getStartTime() : LocalDateTime.MAX)
                                    .thenComparing(e -> e.getTitle() != null ? e.getTitle() : "")
                                    .thenComparing(e -> e.getId() != null ? e.getId().toString() : ""))
                            .limit(MAX_TODAY_EVENTS)
                            .map(this::toEventSummary)
                            .collect(Collectors.toList());
                }
            } catch (Exception e) {
                log.warn("Failed to fetch calendar entries for userId [{}]: {}", userId, e.getMessage());
            }
        }

        String summary;
        if (todayEventsCount == 0 && upcomingEventsCount == 0) {
            summary = "Calendar context summary: Clear schedule for today.";
        } else if (todayEventsCount > 0) {
            summary = String.format("Calendar context summary: %d event(s) scheduled for today.", todayEventsCount);
        } else {
            summary = String.format("Calendar context summary: No events today, %d upcoming event(s).", upcomingEventsCount);
        }

        return CalendarContext.builder()
                .todayEventsCount(todayEventsCount)
                .upcomingEventsCount(upcomingEventsCount)
                .todayEvents(todayEvents)
                .summary(summary)
                .build();
    }

    private EventSummary toEventSummary(CalendarEntry entry) {
        return EventSummary.builder()
                .id(entry.getId() != null ? entry.getId().toString() : null)
                .title(entry.getTitle())
                .startTime(entry.getStartTime() != null ? entry.getStartTime().toString() : null)
                .endTime(entry.getEndTime() != null ? entry.getEndTime().toString() : null)
                .location(entry.getLocation())
                .build();
    }
}
