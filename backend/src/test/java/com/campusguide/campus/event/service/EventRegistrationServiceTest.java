package com.campusguide.campus.event.service;

import com.campusguide.campus.event.dto.EventResponse;
import com.campusguide.campus.event.entity.Event;
import com.campusguide.campus.event.entity.EventStatus;
import com.campusguide.campus.event.entity.EventType;
import com.campusguide.campus.event.repository.EventRepository;
import com.campusguide.common.exception.BadRequestException;
import com.campusguide.common.exception.ResourceNotFoundException;
import com.campusguide.common.exception.UnauthorisedException;
import com.campusguide.personal.notification.service.interfaces.NotificationService;
import com.campusguide.platform.user.entity.Role;
import com.campusguide.platform.user.entity.User;
import com.campusguide.platform.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.campusguide.platform.user.service.CurrentUserService;

@ExtendWith(MockitoExtension.class)
class EventRegistrationServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private EventRegistrationService registrationService;

    private UserDetails studentUserDetails;
    private User studentUser;
    private Event activeEvent;
    private UUID eventId;

    @BeforeEach
    void setUp() {
        eventId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        studentUserDetails = org.springframework.security.core.userdetails.User.withUsername("student@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        studentUser = User.builder()
                .id("user-student")
                .email("student@campusguide.com")
                .role(Role.STUDENT)
                .build();

        lenient().when(currentUserService.getCurrentUser(studentUserDetails)).thenReturn(studentUser);

        activeEvent = Event.builder()
                .id(eventId)
                .title("Annual Hackathon")
                .slug("annual-hackathon")
                .description("Coding challenge.")
                .councilId(UUID.randomUUID())
                .venue("Seminar Hall")
                .eventType(EventType.HACKATHON)
                .status(EventStatus.PUBLISHED)
                .startTime(now.plusDays(5))
                .endTime(now.plusDays(6))
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    @Test
    void registerForEvent_Successful() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(activeEvent));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        EventResponse response = registrationService.registerForEvent(eventId, studentUserDetails);

        assertNotNull(response);
        assertEquals("Annual Hackathon", response.getTitle());
        verify(eventRepository).findById(eventId);
    }

    @Test
    void registerForEvent_ThrowsUnauthorisedException_WhenNullUserDetails() {
        when(currentUserService.getCurrentUser(null)).thenThrow(new UnauthorisedException("User is not authenticated"));
        assertThrows(UnauthorisedException.class, () -> registrationService.registerForEvent(eventId, null));
    }

    @Test
    void registerForEvent_ThrowsResourceNotFoundException_WhenEventNotFound() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> registrationService.registerForEvent(eventId, studentUserDetails));
    }

    @Test
    void registerForEvent_ThrowsBadRequestException_WhenEventCancelled() {
        activeEvent.setStatus(EventStatus.CANCELLED);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(activeEvent));

        assertThrows(BadRequestException.class, () -> registrationService.registerForEvent(eventId, studentUserDetails));
    }
}
