package com.campusguide.campus.event.service;

import com.campusguide.campus.council.exception.CouncilNotFoundException;
import com.campusguide.campus.council.repository.CouncilRepository;
import com.campusguide.campus.event.dto.CreateEventRequest;
import com.campusguide.campus.event.dto.EventResponse;
import com.campusguide.campus.event.dto.UpdateEventRequest;
import com.campusguide.campus.event.dto.UpdateEventStatusRequest;
import com.campusguide.campus.event.entity.Event;
import com.campusguide.campus.event.entity.EventStatus;
import com.campusguide.campus.event.exception.DuplicateEventSlugException;
import com.campusguide.campus.event.exception.EventNotFoundException;
import com.campusguide.campus.event.mapper.EventMapper;
import com.campusguide.campus.event.repository.EventRepository;
import com.campusguide.campus.event.validation.EventValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final CouncilRepository councilRepository;
    private final EventMapper eventMapper;
    private final EventValidator eventValidator;

    public EventResponse createEvent(CreateEventRequest request) {
        if (!councilRepository.existsById(request.getCouncilId())) {
            throw new CouncilNotFoundException("Council not found with ID: " + request.getCouncilId());
        }

        if (eventRepository.existsBySlug(request.getSlug())) {
            throw new DuplicateEventSlugException("Event with slug '" + request.getSlug() + "' already exists");
        }

        eventValidator.validate(
                request.getStartTime(),
                request.getEndTime(),
                request.getRegistrationRequired(),
                request.getRegistrationStart(),
                request.getRegistrationEnd(),
                request.getCapacity()
        );

        Event event = eventMapper.toEntity(request);
        Event saved = eventRepository.save(event);
        return eventMapper.toResponse(saved);
    }

    public List<EventResponse> getPublicUpcomingEvents() {
        List<Event> events = eventRepository.findByStatusAndEndTimeGreaterThanEqualOrderByStartTimeAsc(
                EventStatus.PUBLISHED,
                LocalDateTime.now()
        );
        return eventMapper.toResponseList(events);
    }

    public EventResponse getEventById(UUID id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + id));
        return eventMapper.toResponse(event);
    }

    public EventResponse getEventBySlug(String slug) {
        Event event = eventRepository.findBySlug(slug)
                .orElseThrow(() -> new EventNotFoundException("Event not found with slug: " + slug));
        return eventMapper.toResponse(event);
    }

    public List<EventResponse> getEventsByCouncil(UUID councilId) {
        if (!councilRepository.existsById(councilId)) {
            throw new CouncilNotFoundException("Council not found with ID: " + councilId);
        }

        List<Event> events = eventRepository.findByCouncilId(councilId);
        return eventMapper.toResponseList(events);
    }

    public EventResponse updateEvent(UUID id, UpdateEventRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + id));

        if (eventRepository.existsBySlugAndIdNot(request.getSlug(), id)) {
            throw new DuplicateEventSlugException("Event with slug '" + request.getSlug() + "' already exists");
        }

        eventValidator.validate(
                request.getStartTime(),
                request.getEndTime(),
                request.getRegistrationRequired(),
                request.getRegistrationStart(),
                request.getRegistrationEnd(),
                request.getCapacity()
        );

        eventMapper.updateEntityFromRequest(event, request);
        Event updated = eventRepository.save(event);
        return eventMapper.toResponse(updated);
    }

    public EventResponse updateEventStatus(UUID id, UpdateEventStatusRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + id));

        event.setStatus(request.getStatus());
        event.setUpdatedAt(Instant.now());

        Event updated = eventRepository.save(event);
        return eventMapper.toResponse(updated);
    }

    public void deleteEvent(UUID id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + id));

        eventRepository.delete(event);
    }
}
