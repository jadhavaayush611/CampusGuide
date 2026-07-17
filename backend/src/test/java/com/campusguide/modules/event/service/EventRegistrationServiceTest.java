package com.campusguide.modules.event.service;

import com.campusguide.exception.BadRequestException;
import com.campusguide.exception.ConflictException;
import com.campusguide.exception.ResourceNotFoundException;
import com.campusguide.exception.UnauthorisedException;
import com.campusguide.modules.event.dto.EventResponse;
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
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventRegistrationServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private com.campusguide.modules.notification.service.interfaces.NotificationService notificationService;


    @InjectMocks
    private EventRegistrationService eventRegistrationService;

    private UserDetails studentUserDetails;
    private User studentUser;
    private Event activeEvent;
    private Event deletedEvent;
    private Event cancelledEvent;

    @BeforeEach
    void setUp() {
        studentUserDetails = org.springframework.security.core.userdetails.User.withUsername("student@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        studentUser = User.builder()
                .id("user-student")
                .email("student@campusguide.com")
                .role(Role.STUDENT)
                .build();

        LocalDateTime now = LocalDateTime.now();

        activeEvent = Event.builder()
                .id("event-123")
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
                .registeredUserIds(new ArrayList<>())
                .isCancelled(false)
                .isDeleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        deletedEvent = Event.builder()
                .id("event-999")
                .title("Deleted Event")
                .isDeleted(true)
                .build();

        cancelledEvent = Event.builder()
                .id("event-456")
                .title("Cancelled Event")
                .startTime(now.plusDays(5))
                .endTime(now.plusDays(6))
                .registrationDeadline(now.plusDays(3))
                .isCancelled(true)
                .isDeleted(false)
                .build();
    }

    // --- Registration Tests ---

    @Test
    void registerForEvent_Successful() {
        when(eventRepository.findById("event-123")).thenReturn(Optional.of(activeEvent));
        when(userRepository.findByEmail("student@campusguide.com")).thenReturn(Optional.of(studentUser));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        EventResponse response = eventRegistrationService.registerForEvent("event-123", studentUserDetails);

        assertNotNull(response);
        assertEquals(1, response.getAttendeeCount());
        assertTrue(activeEvent.getRegisteredUserIds().contains("user-student"));

        verify(eventRepository).findById("event-123");
        verify(userRepository).findByEmail("student@campusguide.com");
        verify(eventRepository).save(activeEvent);
    }

    @Test
    void registerForEvent_Unauthenticated() {
        assertThrows(UnauthorisedException.class, 
                () -> eventRegistrationService.registerForEvent("event-123", null));
    }

    @Test
    void registerForEvent_DuplicateRegistration() {
        activeEvent.getRegisteredUserIds().add("user-student");
        activeEvent.setAttendeeCount(1);

        when(eventRepository.findById("event-123")).thenReturn(Optional.of(activeEvent));
        when(userRepository.findByEmail("student@campusguide.com")).thenReturn(Optional.of(studentUser));

        assertThrows(ConflictException.class, 
                () -> eventRegistrationService.registerForEvent("event-123", studentUserDetails));

        verify(eventRepository).findById("event-123");
        verify(userRepository).findByEmail("student@campusguide.com");
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void registerForEvent_EventNotFound() {
        when(eventRepository.findById("event-123")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, 
                () -> eventRegistrationService.registerForEvent("event-123", studentUserDetails));

        verify(eventRepository).findById("event-123");
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void registerForEvent_UserNotFound() {
        when(eventRepository.findById("event-123")).thenReturn(Optional.of(activeEvent));
        when(userRepository.findByEmail("student@campusguide.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, 
                () -> eventRegistrationService.registerForEvent("event-123", studentUserDetails));

        verify(eventRepository).findById("event-123");
        verify(userRepository).findByEmail("student@campusguide.com");
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void registerForEvent_EventCancelled() {
        when(eventRepository.findById("event-456")).thenReturn(Optional.of(cancelledEvent));

        assertThrows(BadRequestException.class, 
                () -> eventRegistrationService.registerForEvent("event-456", studentUserDetails));

        verify(eventRepository).findById("event-456");
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void registerForEvent_RegistrationDeadlinePassed() {
        activeEvent.setRegistrationDeadline(LocalDateTime.now().minusDays(1));

        when(eventRepository.findById("event-123")).thenReturn(Optional.of(activeEvent));
        when(userRepository.findByEmail("student@campusguide.com")).thenReturn(Optional.of(studentUser));

        assertThrows(BadRequestException.class, 
                () -> eventRegistrationService.registerForEvent("event-123", studentUserDetails));

        verify(eventRepository).findById("event-123");
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void registerForEvent_EventAlreadyStarted() {
        activeEvent.setStartTime(LocalDateTime.now().minusDays(1));
        activeEvent.setRegistrationDeadline(LocalDateTime.now().minusDays(2));

        when(eventRepository.findById("event-123")).thenReturn(Optional.of(activeEvent));
        when(userRepository.findByEmail("student@campusguide.com")).thenReturn(Optional.of(studentUser));

        assertThrows(BadRequestException.class, 
                () -> eventRegistrationService.registerForEvent("event-123", studentUserDetails));

        verify(eventRepository).findById("event-123");
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void registerForEvent_CapacityReached() {
        activeEvent.setMaxParticipants(1);
        activeEvent.getRegisteredUserIds().add("user-other");
        activeEvent.setAttendeeCount(1);

        when(eventRepository.findById("event-123")).thenReturn(Optional.of(activeEvent));
        when(userRepository.findByEmail("student@campusguide.com")).thenReturn(Optional.of(studentUser));

        assertThrows(BadRequestException.class, 
                () -> eventRegistrationService.registerForEvent("event-123", studentUserDetails));

        verify(eventRepository).findById("event-123");
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void registerForEvent_SoftDeletedEvent() {
        when(eventRepository.findById("event-999")).thenReturn(Optional.of(deletedEvent));

        assertThrows(ResourceNotFoundException.class, 
                () -> eventRegistrationService.registerForEvent("event-999", studentUserDetails));

        verify(eventRepository).findById("event-999");
        verify(eventRepository, never()).save(any(Event.class));
    }

    // --- Cancellation Tests ---

    @Test
    void cancelRegistration_Successful() {
        activeEvent.getRegisteredUserIds().add("user-student");
        activeEvent.setAttendeeCount(1);

        when(eventRepository.findById("event-123")).thenReturn(Optional.of(activeEvent));
        when(userRepository.findByEmail("student@campusguide.com")).thenReturn(Optional.of(studentUser));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        EventResponse response = eventRegistrationService.cancelRegistration("event-123", studentUserDetails);

        assertNotNull(response);
        assertEquals(0, response.getAttendeeCount());
        assertFalse(activeEvent.getRegisteredUserIds().contains("user-student"));

        verify(eventRepository).findById("event-123");
        verify(userRepository).findByEmail("student@campusguide.com");
        verify(eventRepository).save(activeEvent);
    }

    @Test
    void cancelRegistration_Unauthenticated() {
        assertThrows(UnauthorisedException.class, 
                () -> eventRegistrationService.cancelRegistration("event-123", null));
    }

    @Test
    void cancelRegistration_UserNotRegistered() {
        when(eventRepository.findById("event-123")).thenReturn(Optional.of(activeEvent));
        when(userRepository.findByEmail("student@campusguide.com")).thenReturn(Optional.of(studentUser));

        assertThrows(BadRequestException.class, 
                () -> eventRegistrationService.cancelRegistration("event-123", studentUserDetails));

        verify(eventRepository).findById("event-123");
        verify(userRepository).findByEmail("student@campusguide.com");
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void cancelRegistration_EventNotFound() {
        when(eventRepository.findById("event-123")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, 
                () -> eventRegistrationService.cancelRegistration("event-123", studentUserDetails));

        verify(eventRepository).findById("event-123");
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void cancelRegistration_SoftDeletedEvent() {
        when(eventRepository.findById("event-999")).thenReturn(Optional.of(deletedEvent));

        assertThrows(ResourceNotFoundException.class, 
                () -> eventRegistrationService.cancelRegistration("event-999", studentUserDetails));

        verify(eventRepository).findById("event-999");
        verify(eventRepository, never()).save(any(Event.class));
    }

    // --- Registration Status Tests ---

    @Test
    void isUserRegistered_UserRegistered() {
        activeEvent.getRegisteredUserIds().add("user-student");

        when(eventRepository.findById("event-123")).thenReturn(Optional.of(activeEvent));

        assertTrue(eventRegistrationService.isUserRegistered("event-123", "user-student"));
        verify(eventRepository).findById("event-123");
    }

    @Test
    void isUserRegistered_UserNotRegistered() {
        when(eventRepository.findById("event-123")).thenReturn(Optional.of(activeEvent));

        assertFalse(eventRegistrationService.isUserRegistered("event-123", "user-student"));
        verify(eventRepository).findById("event-123");
    }

    @Test
    void isUserRegistered_WithUserDetails_UserRegistered() {
        activeEvent.getRegisteredUserIds().add("user-student");

        when(userRepository.findByEmail("student@campusguide.com")).thenReturn(Optional.of(studentUser));
        when(eventRepository.findById("event-123")).thenReturn(Optional.of(activeEvent));

        assertTrue(eventRegistrationService.isUserRegistered("event-123", studentUserDetails));
        verify(userRepository).findByEmail("student@campusguide.com");
        verify(eventRepository).findById("event-123");
    }

    @Test
    void isUserRegistered_WithUserDetails_UserNotRegistered() {
        when(userRepository.findByEmail("student@campusguide.com")).thenReturn(Optional.of(studentUser));
        when(eventRepository.findById("event-123")).thenReturn(Optional.of(activeEvent));

        assertFalse(eventRegistrationService.isUserRegistered("event-123", studentUserDetails));
        verify(userRepository).findByEmail("student@campusguide.com");
        verify(eventRepository).findById("event-123");
    }

    @Test
    void isUserRegistered_WithUserDetails_Unauthenticated() {
        assertThrows(UnauthorisedException.class,
                () -> eventRegistrationService.isUserRegistered("event-123", (UserDetails) null));
    }

    // --- Registered Users Tests ---

    @Test
    void getRegisteredUsers_RetrieveRegisteredUserIds() {
        activeEvent.getRegisteredUserIds().add("user-student");
        activeEvent.getRegisteredUserIds().add("user-other");

        when(eventRepository.findById("event-123")).thenReturn(Optional.of(activeEvent));

        List<String> userIds = eventRegistrationService.getRegisteredUsers("event-123");

        assertEquals(2, userIds.size());
        assertTrue(userIds.contains("user-student"));
        assertTrue(userIds.contains("user-other"));
        verify(eventRepository).findById("event-123");
    }

    @Test
    void getRegisteredUsers_EmptyRegistrationList() {
        when(eventRepository.findById("event-123")).thenReturn(Optional.of(activeEvent));

        List<String> userIds = eventRegistrationService.getRegisteredUsers("event-123");

        assertNotNull(userIds);
        assertTrue(userIds.isEmpty());
        verify(eventRepository).findById("event-123");
    }
}
