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
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final CouncilRepository councilRepository;
    private final UserRepository userRepository;

    /**
     * Creates a new event.
     *
     * @param userDetails the authenticated user details
     * @param request the request containing details of the event to create
     * @return the created event details
     */
    public EventResponse createEvent(UserDetails userDetails, CreateEventRequest request) {
        if (userDetails == null) {
            throw new UnauthorisedException("User is not authenticated");
        }

        if (!councilRepository.existsById(request.getCouncilId())) {
            throw new ResourceNotFoundException("Council not found with id: " + request.getCouncilId());
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userDetails.getUsername()));

        validateEventTimes(request.getStartTime(), request.getEndTime(), request.getRegistrationDeadline());

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .councilId(request.getCouncilId())
                .organizerId(user.getId())
                .location(request.getLocation())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .registrationDeadline(request.getRegistrationDeadline())
                .maxParticipants(request.getMaxParticipants())
                .attendeeCount(0)
                .imageUrl(request.getImageUrl())
                .isCancelled(false)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        event = eventRepository.save(event);
        return toEventResponse(event);
    }

    /**
     * Updates an existing event.
     *
     * @param userDetails the authenticated user details
     * @param eventId the ID of the event to update
     * @param request the request containing updated fields
     * @return the updated event details
     */
    public EventResponse updateEvent(UserDetails userDetails, String eventId, UpdateEventRequest request) {
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

        boolean isOrganizer = event.getOrganizerId().equals(user.getId());
        boolean isSuperAdmin = user.getRole() == Role.SUPER_ADMIN;

        if (!isOrganizer && !isSuperAdmin) {
            throw new AccessDeniedException("You are not authorized to update this event");
        }

        LocalDateTime newStartTime = request.getStartTime() != null ? request.getStartTime() : event.getStartTime();
        LocalDateTime newEndTime = request.getEndTime() != null ? request.getEndTime() : event.getEndTime();
        LocalDateTime newRegistrationDeadline = request.getRegistrationDeadline() != null ? request.getRegistrationDeadline() : event.getRegistrationDeadline();

        validateEventTimes(newStartTime, newEndTime, newRegistrationDeadline);

        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getLocation() != null) {
            event.setLocation(request.getLocation());
        }
        if (request.getStartTime() != null) {
            event.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            event.setEndTime(request.getEndTime());
        }
        if (request.getRegistrationDeadline() != null) {
            event.setRegistrationDeadline(request.getRegistrationDeadline());
        }
        if (request.getMaxParticipants() != null) {
            event.setMaxParticipants(request.getMaxParticipants());
        }
        if (request.getImageUrl() != null) {
            event.setImageUrl(request.getImageUrl());
        }

        event.setUpdatedAt(LocalDateTime.now());
        event = eventRepository.save(event);
        return toEventResponse(event);
    }

    /**
     * Soft deletes an event.
     *
     * @param userDetails the authenticated user details
     * @param eventId the ID of the event to delete
     */
    public void deleteEvent(UserDetails userDetails, String eventId) {
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

        boolean isOrganizer = event.getOrganizerId().equals(user.getId());
        boolean isSuperAdmin = user.getRole() == Role.SUPER_ADMIN;

        if (!isOrganizer && !isSuperAdmin) {
            throw new AccessDeniedException("You are not authorized to delete this event");
        }

        event.setIsDeleted(true);
        event.setUpdatedAt(LocalDateTime.now());
        eventRepository.save(event);
    }

    /**
     * Retrieves an event by its ID.
     *
     * @param eventId the ID of the event to retrieve
     * @return the event details
     */
    public EventResponse getEventById(String eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        if (Boolean.TRUE.equals(event.getIsDeleted())) {
            throw new ResourceNotFoundException("Event not found with id: " + eventId);
        }

        return toEventResponse(event);
    }

    /**
     * Retrieves active events for a specific council.
     *
     * @param councilId the ID of the council
     * @return a list of active event summaries
     */
    public List<EventSummaryResponse> getEventsByCouncil(String councilId) {
        if (!councilRepository.existsById(councilId)) {
            throw new ResourceNotFoundException("Council not found with id: " + councilId);
        }

        return eventRepository.findByCouncilIdAndIsDeletedFalse(councilId).stream()
                .map(this::toEventSummaryResponse)
                .toList();
    }

    /**
     * Retrieves upcoming active and non-cancelled events.
     *
     * @return a list of upcoming event summaries
     */
    public List<EventSummaryResponse> getUpcomingEvents() {
        return eventRepository.findByIsDeletedFalseAndIsCancelledFalseAndStartTimeGreaterThanEqualOrderByStartTimeAsc(LocalDateTime.now()).stream()
                .map(this::toEventSummaryResponse)
                .toList();
    }

    /**
     * Retrieves all active events in the system.
     *
     * @return a list of all active event summaries
     */
    public List<EventSummaryResponse> getAllActiveEvents() {
        return eventRepository.findByIsDeletedFalseOrderByStartTimeAsc().stream()
                .map(this::toEventSummaryResponse)
                .toList();
    }

    private void validateEventTimes(LocalDateTime startTime, LocalDateTime endTime, LocalDateTime registrationDeadline) {
        if (startTime != null && endTime != null && !endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time must be after start time.");
        }
        if (registrationDeadline != null && startTime != null && registrationDeadline.isAfter(startTime)) {
            throw new IllegalArgumentException("Registration deadline must be before event start time.");
        }
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

    private EventSummaryResponse toEventSummaryResponse(Event event) {
        if (event == null) {
            return null;
        }
        return EventSummaryResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .councilId(event.getCouncilId())
                .location(event.getLocation())
                .startTime(event.getStartTime())
                .attendeeCount(event.getAttendeeCount())
                .maxParticipants(event.getMaxParticipants())
                .imageUrl(event.getImageUrl())
                .build();
    }
}
