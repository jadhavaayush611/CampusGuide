package com.campusguide.modules.event.service;

import com.campusguide.exception.ResourceNotFoundException;
import com.campusguide.exception.UnauthorisedException;
import com.campusguide.modules.council.repository.CouncilRepository;
import com.campusguide.modules.event.dto.CreateEventRequest;
import com.campusguide.modules.event.dto.EventResponse;
import com.campusguide.modules.event.dto.EventSummaryResponse;
import com.campusguide.modules.event.dto.UpdateEventRequest;
import com.campusguide.modules.event.entity.Event;
import com.campusguide.modules.event.repository.EventRepository;
import com.campusguide.modules.user.entity.Role;
import com.campusguide.modules.user.entity.User;
import com.campusguide.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CouncilRepository councilRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EventService eventService;

    private UserDetails organizerUserDetails;
    private UserDetails adminUserDetails;
    private UserDetails studentUserDetails;

    private User organizerUser;
    private User adminUser;
    private User studentUser;

    private CreateEventRequest createRequest;
    private Event activeEvent;
    private Event deletedEvent;

    @BeforeEach
    void setUp() {
        organizerUserDetails = org.springframework.security.core.userdetails.User.withUsername("organizer@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_COUNCIL_ADMIN")))
                .build();

        adminUserDetails = org.springframework.security.core.userdetails.User.withUsername("admin@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_SUPER_ADMIN")))
                .build();

        studentUserDetails = org.springframework.security.core.userdetails.User.withUsername("student@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        organizerUser = User.builder()
                .id("user-organizer")
                .email("organizer@campusguide.com")
                .role(Role.COUNCIL_ADMIN)
                .build();

        adminUser = User.builder()
                .id("user-admin")
                .email("admin@campusguide.com")
                .role(Role.SUPER_ADMIN)
                .build();

        studentUser = User.builder()
                .id("user-student")
                .email("student@campusguide.com")
                .role(Role.STUDENT)
                .build();

        LocalDateTime now = LocalDateTime.now();

        createRequest = CreateEventRequest.builder()
                .title("Annual Hackathon")
                .description("A 24-hour coding challenge.")
                .councilId("council-123")
                .location("Main Seminar Hall")
                .startTime(now.plusDays(5))
                .endTime(now.plusDays(6))
                .registrationDeadline(now.plusDays(3))
                .maxParticipants(100)
                .imageUrl("http://example.com/hackathon.png")
                .build();

        activeEvent = Event.builder()
                .id("event-789")
                .title("Annual Hackathon")
                .description("A 24-hour coding challenge.")
                .councilId("council-123")
                .organizerId("user-organizer")
                .location("Main Seminar Hall")
                .startTime(now.plusDays(5))
                .endTime(now.plusDays(6))
                .registrationDeadline(now.plusDays(3))
                .maxParticipants(100)
                .attendeeCount(0)
                .imageUrl("http://example.com/hackathon.png")
                .isCancelled(false)
                .isDeleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        deletedEvent = Event.builder()
                .id("event-999")
                .title("Deleted Event")
                .councilId("council-123")
                .organizerId("user-organizer")
                .isDeleted(true)
                .build();
    }

    // --- createEvent() Tests ---

    @Test
    void createEvent_Successful() {
        when(councilRepository.existsById("council-123")).thenReturn(true);
        when(userRepository.findByEmail("organizer@campusguide.com")).thenReturn(Optional.of(organizerUser));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        EventResponse response = eventService.createEvent(organizerUserDetails, createRequest);

        assertNotNull(response);
        assertEquals("Annual Hackathon", response.getTitle());
        assertEquals("user-organizer", response.getOrganizerId());
        assertEquals("council-123", response.getCouncilId());
        assertFalse(response.getIsCancelled());
        assertEquals(0, response.getAttendeeCount());

        verify(councilRepository).existsById("council-123");
        verify(userRepository).findByEmail("organizer@campusguide.com");
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void createEvent_ThrowsResourceNotFoundException_WhenCouncilNotFound() {
        when(councilRepository.existsById("council-123")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> eventService.createEvent(organizerUserDetails, createRequest));

        verify(councilRepository).existsById("council-123");
        verify(userRepository, never()).findByEmail(anyString());
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void createEvent_ThrowsResourceNotFoundException_WhenOrganizerNotFound() {
        when(councilRepository.existsById("council-123")).thenReturn(true);
        when(userRepository.findByEmail("organizer@campusguide.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> eventService.createEvent(organizerUserDetails, createRequest));

        verify(councilRepository).existsById("council-123");
        verify(userRepository).findByEmail("organizer@campusguide.com");
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void createEvent_ThrowsIllegalArgumentException_WhenInvalidEndTime() {
        when(councilRepository.existsById("council-123")).thenReturn(true);
        when(userRepository.findByEmail("organizer@campusguide.com")).thenReturn(Optional.of(organizerUser));

        LocalDateTime now = LocalDateTime.now();
        createRequest.setStartTime(now.plusDays(2));
        createRequest.setEndTime(now.plusDays(1)); // end before start

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> eventService.createEvent(organizerUserDetails, createRequest));
        assertEquals("End time must be after start time.", exception.getMessage());

        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void createEvent_ThrowsIllegalArgumentException_WhenInvalidRegistrationDeadline() {
        when(councilRepository.existsById("council-123")).thenReturn(true);
        when(userRepository.findByEmail("organizer@campusguide.com")).thenReturn(Optional.of(organizerUser));

        LocalDateTime now = LocalDateTime.now();
        createRequest.setStartTime(now.plusDays(2));
        createRequest.setEndTime(now.plusDays(3));
        createRequest.setRegistrationDeadline(now.plusDays(4)); // deadline after start

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> eventService.createEvent(organizerUserDetails, createRequest));
        assertEquals("Registration deadline must be before event start time.", exception.getMessage());

        verify(eventRepository, never()).save(any(Event.class));
    }

    // --- updateEvent() Tests ---

    @Test
    void updateEvent_Successful_AsOrganizer() {
        UpdateEventRequest updateRequest = UpdateEventRequest.builder()
                .title("Updated Title")
                .description("Updated Description")
                .build();

        when(eventRepository.findById("event-789")).thenReturn(Optional.of(activeEvent));
        when(userRepository.findByEmail("organizer@campusguide.com")).thenReturn(Optional.of(organizerUser));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        EventResponse response = eventService.updateEvent(organizerUserDetails, "event-789", updateRequest);

        assertNotNull(response);
        assertEquals("Updated Title", response.getTitle());
        assertEquals("Updated Description", response.getDescription());
        assertEquals("Main Seminar Hall", response.getLocation()); // remains unchanged

        verify(eventRepository).findById("event-789");
        verify(userRepository).findByEmail("organizer@campusguide.com");
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void updateEvent_Successful_AsSuperAdmin() {
        UpdateEventRequest updateRequest = UpdateEventRequest.builder()
                .title("SuperAdmin Updated Title")
                .build();

        when(eventRepository.findById("event-789")).thenReturn(Optional.of(activeEvent));
        when(userRepository.findByEmail("admin@campusguide.com")).thenReturn(Optional.of(adminUser));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        EventResponse response = eventService.updateEvent(adminUserDetails, "event-789", updateRequest);

        assertNotNull(response);
        assertEquals("SuperAdmin Updated Title", response.getTitle());

        verify(eventRepository).findById("event-789");
        verify(userRepository).findByEmail("admin@campusguide.com");
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void updateEvent_ThrowsAccessDeniedException_WhenUnauthorizedUser() {
        UpdateEventRequest updateRequest = UpdateEventRequest.builder()
                .title("Unauthorized Title")
                .build();

        when(eventRepository.findById("event-789")).thenReturn(Optional.of(activeEvent));
        when(userRepository.findByEmail("student@campusguide.com")).thenReturn(Optional.of(studentUser));

        assertThrows(AccessDeniedException.class, 
                () -> eventService.updateEvent(studentUserDetails, "event-789", updateRequest));

        verify(eventRepository).findById("event-789");
        verify(userRepository).findByEmail("student@campusguide.com");
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void updateEvent_ThrowsResourceNotFoundException_WhenDeletedEvent() {
        UpdateEventRequest updateRequest = UpdateEventRequest.builder()
                .title("Deleted Update")
                .build();

        when(eventRepository.findById("event-999")).thenReturn(Optional.of(deletedEvent));

        assertThrows(ResourceNotFoundException.class, 
                () -> eventService.updateEvent(organizerUserDetails, "event-999", updateRequest));

        verify(eventRepository).findById("event-999");
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void updateEvent_PartialUpdate_ValidatesMergedTimes() {
        LocalDateTime now = LocalDateTime.now();
        UpdateEventRequest updateRequest = UpdateEventRequest.builder()
                .startTime(now.plusDays(10)) // valid by itself, but end time is still at now.plusDays(6)
                .build();

        when(eventRepository.findById("event-789")).thenReturn(Optional.of(activeEvent));
        when(userRepository.findByEmail("organizer@campusguide.com")).thenReturn(Optional.of(organizerUser));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> eventService.updateEvent(organizerUserDetails, "event-789", updateRequest));
        assertEquals("End time must be after start time.", exception.getMessage());

        verify(eventRepository, never()).save(any(Event.class));
    }

    // --- deleteEvent() Tests ---

    @Test
    void deleteEvent_Successful_AsOrganizer() {
        when(eventRepository.findById("event-789")).thenReturn(Optional.of(activeEvent));
        when(userRepository.findByEmail("organizer@campusguide.com")).thenReturn(Optional.of(organizerUser));

        eventService.deleteEvent(organizerUserDetails, "event-789");

        assertTrue(activeEvent.getIsDeleted());
        verify(eventRepository).findById("event-789");
        verify(eventRepository).save(activeEvent);
    }

    @Test
    void deleteEvent_Successful_AsSuperAdmin() {
        when(eventRepository.findById("event-789")).thenReturn(Optional.of(activeEvent));
        when(userRepository.findByEmail("admin@campusguide.com")).thenReturn(Optional.of(adminUser));

        eventService.deleteEvent(adminUserDetails, "event-789");

        assertTrue(activeEvent.getIsDeleted());
        verify(eventRepository).findById("event-789");
        verify(eventRepository).save(activeEvent);
    }

    @Test
    void deleteEvent_ThrowsAccessDeniedException_WhenUnauthorizedUser() {
        when(eventRepository.findById("event-789")).thenReturn(Optional.of(activeEvent));
        when(userRepository.findByEmail("student@campusguide.com")).thenReturn(Optional.of(studentUser));

        assertThrows(AccessDeniedException.class, 
                () -> eventService.deleteEvent(studentUserDetails, "event-789"));

        verify(eventRepository, never()).save(any(Event.class));
    }

    // --- Query Tests ---

    @Test
    void getEventById_Successful() {
        when(eventRepository.findById("event-789")).thenReturn(Optional.of(activeEvent));

        EventResponse response = eventService.getEventById("event-789");

        assertNotNull(response);
        assertEquals("event-789", response.getId());
        assertEquals("Annual Hackathon", response.getTitle());
    }

    @Test
    void getEventById_ThrowsResourceNotFoundException_WhenDeleted() {
        when(eventRepository.findById("event-999")).thenReturn(Optional.of(deletedEvent));

        assertThrows(ResourceNotFoundException.class, () -> eventService.getEventById("event-999"));
    }

    @Test
    void getEventsByCouncil_Successful() {
        when(councilRepository.existsById("council-123")).thenReturn(true);
        when(eventRepository.findByCouncilIdAndIsDeletedFalse("council-123")).thenReturn(List.of(activeEvent));

        List<EventSummaryResponse> responses = eventService.getEventsByCouncil("council-123");

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("event-789", responses.get(0).getId());
        assertEquals("Annual Hackathon", responses.get(0).getTitle());
    }

    @Test
    void getEventsByCouncil_ThrowsResourceNotFoundException_WhenCouncilNotFound() {
        when(councilRepository.existsById("council-123")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> eventService.getEventsByCouncil("council-123"));
    }

    @Test
    void getUpcomingEvents_Successful() {
        when(eventRepository.findByIsDeletedFalseAndIsCancelledFalseAndStartTimeGreaterThanEqualOrderByStartTimeAsc(any(LocalDateTime.class)))
                .thenReturn(List.of(activeEvent));

        List<EventSummaryResponse> responses = eventService.getUpcomingEvents();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("event-789", responses.get(0).getId());
    }

    @Test
    void getAllActiveEvents_Successful() {
        when(eventRepository.findByIsDeletedFalseOrderByStartTimeAsc()).thenReturn(List.of(activeEvent));

        List<EventSummaryResponse> responses = eventService.getAllActiveEvents();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("event-789", responses.get(0).getId());
    }
}
