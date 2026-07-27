package com.campusguide.campus.event.service;

import com.campusguide.campus.council.exception.CouncilNotFoundException;
import com.campusguide.campus.council.repository.CouncilRepository;
import com.campusguide.campus.event.dto.CreateEventRequest;
import com.campusguide.campus.event.dto.EventResponse;
import com.campusguide.campus.event.dto.UpdateEventRequest;
import com.campusguide.campus.event.dto.UpdateEventStatusRequest;
import com.campusguide.campus.event.entity.Event;
import com.campusguide.campus.event.entity.EventStatus;
import com.campusguide.campus.event.entity.EventType;
import com.campusguide.campus.event.exception.DuplicateEventSlugException;
import com.campusguide.campus.event.exception.EventNotFoundException;
import com.campusguide.campus.event.mapper.EventMapper;
import com.campusguide.campus.event.repository.EventRepository;
import com.campusguide.campus.event.validation.EventValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CouncilRepository councilRepository;

    @Spy
    private EventMapper eventMapper = new EventMapper();

    @Spy
    private EventValidator eventValidator = new EventValidator();

    @InjectMocks
    private EventService eventService;

    private UUID councilId;
    private UUID eventId;
    private CreateEventRequest createRequest;
    private UpdateEventRequest updateRequest;
    private Event existingEvent;

    @BeforeEach
    void setUp() {
        councilId = UUID.randomUUID();
        eventId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        createRequest = CreateEventRequest.builder()
                .title("Hackathon 2026")
                .slug("hackathon-2026")
                .description("Annual coding competition.")
                .summary("24h hackathon")
                .councilId(councilId)
                .venue("Hall B")
                .eventType(EventType.HACKATHON)
                .status(EventStatus.DRAFT)
                .registrationRequired(true)
                .registrationStart(now.plusDays(1))
                .registrationEnd(now.plusDays(3))
                .capacity(150)
                .startTime(now.plusDays(5))
                .endTime(now.plusDays(6))
                .bannerUrl("http://example.com/banner.jpg")
                .contactEmail("hack@campus.edu")
                .contactNumber("1234567890")
                .build();

        updateRequest = UpdateEventRequest.builder()
                .title("Updated Hackathon 2026")
                .slug("updated-hackathon-2026")
                .description("Updated description.")
                .venue("Hall C")
                .eventType(EventType.HACKATHON)
                .startTime(now.plusDays(5))
                .endTime(now.plusDays(6))
                .build();

        existingEvent = Event.builder()
                .id(eventId)
                .title("Hackathon 2026")
                .slug("hackathon-2026")
                .description("Annual coding competition.")
                .councilId(councilId)
                .venue("Hall B")
                .eventType(EventType.HACKATHON)
                .status(EventStatus.PUBLISHED)
                .startTime(now.plusDays(5))
                .endTime(now.plusDays(6))
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    @Test
    void createEvent_Successful() {
        when(councilRepository.existsById(councilId)).thenReturn(true);
        when(eventRepository.existsBySlug("hackathon-2026")).thenReturn(false);
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        EventResponse response = eventService.createEvent(createRequest);

        assertNotNull(response);
        assertEquals("Hackathon 2026", response.getTitle());
        assertEquals("hackathon-2026", response.getSlug());
        assertEquals(councilId, response.getCouncilId());
        assertEquals(EventStatus.DRAFT, response.getStatus());

        verify(councilRepository).existsById(councilId);
        verify(eventRepository).existsBySlug("hackathon-2026");
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void createEvent_ThrowsCouncilNotFoundException_WhenCouncilDoesNotExist() {
        when(councilRepository.existsById(councilId)).thenReturn(false);

        assertThrows(CouncilNotFoundException.class, () -> eventService.createEvent(createRequest));
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void createEvent_ThrowsDuplicateEventSlugException_WhenSlugExists() {
        when(councilRepository.existsById(councilId)).thenReturn(true);
        when(eventRepository.existsBySlug("hackathon-2026")).thenReturn(true);

        assertThrows(DuplicateEventSlugException.class, () -> eventService.createEvent(createRequest));
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void getPublicUpcomingEvents_ReturnsPublishedList() {
        when(eventRepository.findByStatusAndEndTimeGreaterThanEqualOrderByStartTimeAsc(eq(EventStatus.PUBLISHED), any(LocalDateTime.class)))
                .thenReturn(List.of(existingEvent));

        List<EventResponse> response = eventService.getPublicUpcomingEvents();

        assertEquals(1, response.size());
        assertEquals("Hackathon 2026", response.get(0).getTitle());
    }

    @Test
    void getEventById_Successful() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(existingEvent));

        EventResponse response = eventService.getEventById(eventId);

        assertNotNull(response);
        assertEquals(eventId, response.getId());
    }

    @Test
    void getEventById_ThrowsEventNotFoundException() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThrows(EventNotFoundException.class, () -> eventService.getEventById(eventId));
    }

    @Test
    void getEventBySlug_Successful() {
        when(eventRepository.findBySlug("hackathon-2026")).thenReturn(Optional.of(existingEvent));

        EventResponse response = eventService.getEventBySlug("hackathon-2026");

        assertNotNull(response);
        assertEquals("hackathon-2026", response.getSlug());
    }

    @Test
    void getEventsByCouncil_Successful() {
        when(councilRepository.existsById(councilId)).thenReturn(true);
        when(eventRepository.findByCouncilId(councilId)).thenReturn(List.of(existingEvent));

        List<EventResponse> response = eventService.getEventsByCouncil(councilId);

        assertEquals(1, response.size());
        assertEquals(councilId, response.get(0).getCouncilId());
    }

    @Test
    void updateEvent_Successful() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(existingEvent));
        when(eventRepository.existsBySlugAndIdNot("updated-hackathon-2026", eventId)).thenReturn(false);
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        EventResponse response = eventService.updateEvent(eventId, updateRequest);

        assertNotNull(response);
        assertEquals("Updated Hackathon 2026", response.getTitle());
        assertEquals("updated-hackathon-2026", response.getSlug());
    }

    @Test
    void updateEventStatus_Successful() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(existingEvent));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateEventStatusRequest statusRequest = new UpdateEventStatusRequest(EventStatus.CANCELLED);
        EventResponse response = eventService.updateEventStatus(eventId, statusRequest);

        assertNotNull(response);
        assertEquals(EventStatus.CANCELLED, response.getStatus());
    }

    @Test
    void deleteEvent_Successful() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(existingEvent));

        eventService.deleteEvent(eventId);

        verify(eventRepository).delete(existingEvent);
    }
}
