package com.campusguide.modules.event.service;

import com.campusguide.exception.BadRequestException;
import com.campusguide.exception.ConflictException;
import com.campusguide.exception.ResourceNotFoundException;
import com.campusguide.exception.UnauthorisedException;
import com.campusguide.modules.event.dto.EventResponse;
import com.campusguide.modules.event.entity.Event;
import com.campusguide.modules.event.repository.EventRepository;
import com.campusguide.modules.user.entity.User;
import com.campusguide.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventRegistrationService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    /**
     * Registers the authenticated user for an event.
     *
     * @param eventId      the ID of the event to register for
     * @param userDetails  the authenticated user details
     * @return the updated event details
     */
    public EventResponse registerForEvent(String eventId, UserDetails userDetails) {
        if (userDetails == null) {
            throw new UnauthorisedException("User is not authenticated");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        if (Boolean.TRUE.equals(event.getIsDeleted())) {
            throw new ResourceNotFoundException("Event not found with id: " + eventId);
        }

        if (Boolean.TRUE.equals(event.getIsCancelled())) {
            throw new BadRequestException("Cannot register for a cancelled event");
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userDetails.getUsername()));

        LocalDateTime now = LocalDateTime.now();

        if (event.getStartTime() != null && now.isAfter(event.getStartTime())) {
            throw new BadRequestException("Cannot register for a past event");
        }

        if (event.getRegistrationDeadline() != null && now.isAfter(event.getRegistrationDeadline())) {
            throw new BadRequestException("Registration deadline has passed");
        }

        if (event.getRegisteredUserIds() == null) {
            event.setRegisteredUserIds(new ArrayList<>());
        }

        if (event.getRegisteredUserIds().contains(user.getId())) {
            throw new ConflictException("User is already registered for this event");
        }

        if (event.getMaxParticipants() != null) {
            int currentAttendees = event.getAttendeeCount() != null ? event.getAttendeeCount() : 0;
            if (currentAttendees >= event.getMaxParticipants()) {
                throw new BadRequestException("Event capacity has been reached");
            }
        }

        event.getRegisteredUserIds().add(user.getId());
        event.setAttendeeCount(event.getRegisteredUserIds().size());
        event.setUpdatedAt(now);

        event = eventRepository.save(event);
        return toEventResponse(event);
    }

    /**
     * Cancels the authenticated user's registration for an event.
     *
     * @param eventId      the ID of the event to cancel registration for
     * @param userDetails  the authenticated user details
     * @return the updated event details
     */
    public EventResponse cancelRegistration(String eventId, UserDetails userDetails) {
        if (userDetails == null) {
            throw new UnauthorisedException("User is not authenticated");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        if (Boolean.TRUE.equals(event.getIsDeleted())) {
            throw new ResourceNotFoundException("Event not found with id: " + eventId);
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userDetails.getUsername()));

        if (event.getRegisteredUserIds() == null || !event.getRegisteredUserIds().contains(user.getId())) {
            throw new BadRequestException("User is not registered for this event");
        }

        event.getRegisteredUserIds().remove(user.getId());
        event.setAttendeeCount(event.getRegisteredUserIds().size());
        event.setUpdatedAt(LocalDateTime.now());

        event = eventRepository.save(event);
        return toEventResponse(event);
    }

    /**
     * Checks if a user is registered for a specific event.
     *
     * @param eventId the ID of the event
     * @param userId  the ID of the user
     * @return true if the user is registered, false otherwise
     */
    public boolean isUserRegistered(String eventId, String userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        if (Boolean.TRUE.equals(event.getIsDeleted())) {
            throw new ResourceNotFoundException("Event not found with id: " + eventId);
        }

        return event.getRegisteredUserIds() != null && event.getRegisteredUserIds().contains(userId);
    }

    /**
     * Checks if a user is registered for a specific event.
     *
     * @param eventId      the ID of the event
     * @param userDetails  the authenticated user details
     * @return true if the user is registered, false otherwise
     */
    public boolean isUserRegistered(String eventId, UserDetails userDetails) {
        if (userDetails == null) {
            throw new UnauthorisedException("User is not authenticated");
        }
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userDetails.getUsername()));
        return isUserRegistered(eventId, user.getId());
    }

    /**
     * Retrieves the IDs of all registered users for a specific event.
     *
     * @param eventId the ID of the event
     * @return a list of registered user IDs
     */
    public List<String> getRegisteredUsers(String eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        if (Boolean.TRUE.equals(event.getIsDeleted())) {
            throw new ResourceNotFoundException("Event not found with id: " + eventId);
        }

        return event.getRegisteredUserIds() != null ? new ArrayList<>(event.getRegisteredUserIds()) : new ArrayList<>();
    }

    private EventResponse toEventResponse(Event event) {
        if (event == null) {
            return null;
        }
        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .councilId(event.getCouncilId())
                .organizerId(event.getOrganizerId())
                .location(event.getLocation())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .registrationDeadline(event.getRegistrationDeadline())
                .maxParticipants(event.getMaxParticipants())
                .attendeeCount(event.getAttendeeCount())
                .imageUrl(event.getImageUrl())
                .isCancelled(event.getIsCancelled())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }
}
