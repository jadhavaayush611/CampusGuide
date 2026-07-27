package com.campusguide.campus.event.repository;

import com.campusguide.campus.event.entity.Event;
import com.campusguide.campus.event.entity.EventStatus;
import com.campusguide.campus.event.entity.EventType;
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
class EventRepositoryIT {

    @Autowired
    private EventRepository eventRepository;

    private UUID eventId1;
    private UUID eventId2;
    private UUID councilId;
    private Event event1;
    private Event event2;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
        eventId1 = UUID.randomUUID();
        eventId2 = UUID.randomUUID();
        councilId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now().withNano(0);

        event1 = Event.builder()
                .id(eventId1)
                .title("Annual Tech Symposium")
                .slug("annual-tech-symposium")
                .description("A premier technology symposium for all students.")
                .summary("Tech symposium summary")
                .councilId(councilId)
                .venue("Main Auditorium")
                .eventType(EventType.SEMINAR)
                .status(EventStatus.PUBLISHED)
                .registrationRequired(true)
                .registrationStart(now.minusDays(5))
                .registrationEnd(now.plusDays(1))
                .capacity(200)
                .startTime(now.plusDays(2))
                .endTime(now.plusDays(3))
                .bannerUrl("http://example.com/banner.png")
                .contactEmail("tech@campus.edu")
                .contactNumber("+1234567890")
                .createdAt(now)
                .updatedAt(now)
                .build();

        event2 = Event.builder()
                .id(eventId2)
                .title("Draft Workshop")
                .slug("draft-workshop")
                .description("Unpublished draft workshop.")
                .councilId(councilId)
                .venue("Room 101")
                .eventType(EventType.WORKSHOP)
                .status(EventStatus.DRAFT)
                .registrationRequired(false)
                .startTime(now.plusDays(5))
                .endTime(now.plusDays(6))
                .createdAt(now)
                .updatedAt(now)
                .build();

        eventRepository.save(event1);
        eventRepository.save(event2);
    }

    @AfterEach
    void tearDown() {
        eventRepository.deleteAll();
    }

    @Test
    void findById_ReturnsEvent() {
        Optional<Event> found = eventRepository.findById(eventId1);
        assertTrue(found.isPresent());
        assertEquals("Annual Tech Symposium", found.get().getTitle());
    }

    @Test
    void findBySlug_ReturnsEvent() {
        Optional<Event> found = eventRepository.findBySlug("annual-tech-symposium");
        assertTrue(found.isPresent());
        assertEquals(eventId1, found.get().getId());
    }

    @Test
    void existsBySlug_ReturnsTrueWhenExists() {
        assertTrue(eventRepository.existsBySlug("annual-tech-symposium"));
        assertFalse(eventRepository.existsBySlug("non-existent-slug"));
    }

    @Test
    void existsBySlugAndIdNot_ReturnsCorrectBoolean() {
        assertTrue(eventRepository.existsBySlugAndIdNot("annual-tech-symposium", eventId2));
        assertFalse(eventRepository.existsBySlugAndIdNot("annual-tech-symposium", eventId1));
    }

    @Test
    void findByCouncilId_ReturnsEventsForCouncil() {
        List<Event> events = eventRepository.findByCouncilId(councilId);
        assertEquals(2, events.size());
    }

    @Test
    void findByStatusAndEndTimeGreaterThanEqualOrderByStartTimeAsc_ReturnsOnlyPublishedUpcoming() {
        List<Event> publishedEvents = eventRepository.findByStatusAndEndTimeGreaterThanEqualOrderByStartTimeAsc(
                EventStatus.PUBLISHED,
                LocalDateTime.now()
        );

        assertEquals(1, publishedEvents.size());
        assertEquals("Annual Tech Symposium", publishedEvents.get(0).getTitle());
    }

    @Test
    void findByTitleContainingIgnoreCase_ReturnsMatchingEvents() {
        List<Event> matches = eventRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrVenueContainingIgnoreCaseOrderByStartTimeAsc(
                "Tech", "Tech", "Tech"
        );

        assertEquals(1, matches.size());
        assertEquals(eventId1, matches.get(0).getId());
    }

    @Test
    void existsByCouncilId_ReturnsTrue() {
        assertTrue(eventRepository.existsByCouncilId(councilId));
        assertFalse(eventRepository.existsByCouncilId(UUID.randomUUID()));
    }
}
