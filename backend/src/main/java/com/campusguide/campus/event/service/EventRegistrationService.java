package com.campusguide.campus.event.service;

import com.campusguide.campus.event.dto.EventResponse;
import com.campusguide.campus.event.entity.Event;
import com.campusguide.campus.event.entity.EventStatus;
import com.campusguide.campus.event.repository.EventRepository;
import com.campusguide.common.exception.BadRequestException;
import com.campusguide.common.exception.ResourceNotFoundException;
import com.campusguide.common.exception.UnauthorisedException;
import com.campusguide.personal.notification.enums.NotificationPriority;
import com.campusguide.personal.notification.enums.NotificationType;
import com.campusguide.personal.notification.service.interfaces.NotificationService;
import com.campusguide.platform.user.entity.User;
import com.campusguide.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventRegistrationService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public EventResponse registerForEvent(UUID eventId, UserDetails userDetails) {
        if (userDetails == null) {
            throw new UnauthorisedException("User is not authenticated");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new BadRequestException("Cannot register for a cancelled event");
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userDetails.getUsername()));

        LocalDateTime now = LocalDateTime.now();

        if (event.getStartTime() != null && now.isAfter(event.getStartTime())) {
            throw new BadRequestException("Cannot register for a past event");
        }

        if (event.getRegistrationEnd() != null && now.isAfter(event.getRegistrationEnd())) {
            throw new BadRequestException("Registration deadline has passed");
        }

        event.setUpdatedAt(now);
        event = eventRepository.save(event);

        notificationService.createNotification(
                user.getId(),
                "Event Registration Confirmed",
                "You have successfully registered for the event: " + event.getTitle(),
                NotificationType.EVENT,
                NotificationPriority.NORMAL,
                null
        );
        return toEventResponse(event);
    }

    public EventResponse cancelRegistration(UUID eventId, UserDetails userDetails) {
        if (userDetails == null) {
            throw new UnauthorisedException("User is not authenticated");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userDetails.getUsername()));

        event.setUpdatedAt(LocalDateTime.now());
        event = eventRepository.save(event);
        return toEventResponse(event);
    }

    public boolean isUserRegistered(UUID eventId, String userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));
        return false;
    }

    public boolean isUserRegistered(UUID eventId, UserDetails userDetails) {
        if (userDetails == null) {
            throw new UnauthorisedException("User is not authenticated");
        }
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userDetails.getUsername()));
        return isUserRegistered(eventId, user.getId());
    }

    public List<String> getRegisteredUsers(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));
        return new ArrayList<>();
    }

    private EventResponse toEventResponse(Event event) {
        if (event == null) {
            return null;
        }
        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .slug(event.getSlug())
                .description(event.getDescription())
                .summary(event.getSummary())
                .councilId(event.getCouncilId())
                .venue(event.getVenue())
                .eventType(event.getEventType())
                .status(event.getStatus())
                .registrationRequired(event.getRegistrationRequired())
                .registrationStart(event.getRegistrationStart())
                .registrationEnd(event.getRegistrationEnd())
                .capacity(event.getCapacity())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .bannerUrl(event.getBannerUrl())
                .contactEmail(event.getContactEmail())
                .contactNumber(event.getContactNumber())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }
}
