package com.campusguide.personal.ai.atlas.context.service;

import com.campusguide.personal.ai.atlas.context.model.CalendarContext;
import com.campusguide.personal.calendar.entity.CalendarEntry;
import com.campusguide.personal.calendar.repository.CalendarEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalendarContextServiceTest {

    @Mock
    private CalendarEntryRepository calendarEntryRepository;

    private CalendarContextService calendarContextService;

    @BeforeEach
    void setUp() {
        calendarContextService = new CalendarContextService(calendarEntryRepository);
    }

    @Test
    void testGetCalendarContext_TodayEvents() {
        String userId = "user-1";
        LocalDateTime todayMorning = LocalDateTime.now().withHour(9).withMinute(0);

        CalendarEntry entry1 = CalendarEntry.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .title("Math Lecture")
                .startTime(todayMorning)
                .endTime(todayMorning.plusHours(1))
                .location("Hall A")
                .build();

        when(calendarEntryRepository.findByUserIdOrderByStartTimeAscEndTimeAsc(userId))
                .thenReturn(List.of(entry1));

        CalendarContext context = calendarContextService.getCalendarContext(userId, null);

        assertNotNull(context);
        assertEquals(1, context.getTodayEventsCount());
        assertEquals(1, context.getTodayEvents().size());
        assertEquals("Math Lecture", context.getTodayEvents().get(0).getTitle());
    }
}
