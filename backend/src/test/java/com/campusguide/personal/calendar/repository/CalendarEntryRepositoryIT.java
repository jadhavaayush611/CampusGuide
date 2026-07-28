package com.campusguide.personal.calendar.repository;

import com.campusguide.personal.calendar.entity.CalendarEntry;
import com.campusguide.personal.calendar.entity.CalendarEntryType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CalendarEntryRepositoryIT {

    @Autowired
    private CalendarEntryRepository calendarEntryRepository;

    private String userId1;
    private String userId2;
    private CalendarEntry entry1;
    private CalendarEntry entry2;
    private CalendarEntry entry3;

    @BeforeEach
    void setUp() {
        calendarEntryRepository.deleteAll();

        userId1 = UUID.randomUUID().toString();
        userId2 = UUID.randomUUID().toString();

        LocalDateTime base = LocalDateTime.of(2026, 8, 10, 10, 0);

        entry1 = CalendarEntry.builder()
                .id(UUID.randomUUID())
                .userId(userId1)
                .title("Math Class")
                .description("Lecture 1")
                .type(CalendarEntryType.ACADEMIC)
                .location("Hall A")
                .startTime(base)
                .endTime(base.plusHours(2))
                .isAllDay(false)
                .color("#FF0000")
                .createdAt(base.minusDays(1))
                .updatedAt(base.minusDays(1))
                .build();

        entry2 = CalendarEntry.builder()
                .id(UUID.randomUUID())
                .userId(userId1)
                .title("Study Group")
                .description("Exam revision")
                .type(CalendarEntryType.PERSONAL)
                .location("Library")
                .startTime(base.plusHours(3))
                .endTime(base.plusHours(5))
                .isAllDay(false)
                .color("#00FF00")
                .createdAt(base.minusDays(1))
                .updatedAt(base.minusDays(1))
                .build();

        entry3 = CalendarEntry.builder()
                .id(UUID.randomUUID())
                .userId(userId2)
                .title("Other User Entry")
                .type(CalendarEntryType.OTHER)
                .startTime(base)
                .endTime(base.plusHours(1))
                .isAllDay(false)
                .createdAt(base.minusDays(1))
                .updatedAt(base.minusDays(1))
                .build();

        calendarEntryRepository.save(entry1);
        calendarEntryRepository.save(entry2);
        calendarEntryRepository.save(entry3);
    }

    @AfterEach
    void tearDown() {
        calendarEntryRepository.deleteAll();
    }

    @Test
    void testSaveAndFindById() {
        Optional<CalendarEntry> found = calendarEntryRepository.findById(entry1.getId());
        assertTrue(found.isPresent());
        assertEquals("Math Class", found.get().getTitle());
        assertEquals(userId1, found.get().getUserId());
    }

    @Test
    void testFindByUserIdOrderByStartTimeAscEndTimeAsc() {
        List<CalendarEntry> entries = calendarEntryRepository.findByUserIdOrderByStartTimeAscEndTimeAsc(userId1);
        assertEquals(2, entries.size());
        assertEquals(entry1.getId(), entries.get(0).getId());
        assertEquals(entry2.getId(), entries.get(1).getId());
    }

    @Test
    void testFindByUserIdAndRangeOverlaps() {
        LocalDateTime base = LocalDateTime.of(2026, 8, 10, 10, 0);
        // Query from 09:00 to 13:00. Entry1 is 10:00-12:00 (overlaps). Entry2 is 13:00-15:00 (does not overlap).
        LocalDateTime queryFrom = base.minusHours(1);
        LocalDateTime queryTo = base.plusHours(3);

        List<CalendarEntry> rangeEntries = calendarEntryRepository
                .findByUserIdAndStartTimeBeforeAndEndTimeAfterOrderByStartTimeAscEndTimeAsc(userId1, queryTo, queryFrom);

        assertEquals(1, rangeEntries.size());
        assertEquals(entry1.getId(), rangeEntries.get(0).getId());
    }

    @Test
    void testFindByIdAndUserId() {
        Optional<CalendarEntry> found = calendarEntryRepository.findByIdAndUserId(entry1.getId(), userId1);
        assertTrue(found.isPresent());

        Optional<CalendarEntry> notFoundOtherUser = calendarEntryRepository.findByIdAndUserId(entry1.getId(), userId2);
        assertFalse(notFoundOtherUser.isPresent());
    }

    @Test
    void testDeleteEntry() {
        calendarEntryRepository.deleteById(entry1.getId());
        Optional<CalendarEntry> found = calendarEntryRepository.findById(entry1.getId());
        assertFalse(found.isPresent());
    }
}
