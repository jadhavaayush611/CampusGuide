package com.campusguide.campus.event.mapper;

import com.campusguide.campus.event.dto.CreateEventRequest;
import com.campusguide.campus.event.dto.EventResponse;
import com.campusguide.campus.event.dto.UpdateEventRequest;
import com.campusguide.campus.event.entity.Event;
import com.campusguide.campus.event.entity.EventStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class EventMapper {

    public Event toEntity(CreateEventRequest request) {
        if (request == null) {
            return null;
        }

        Instant now = Instant.now();
        EventStatus status = request.getStatus() != null ? request.getStatus() : EventStatus.DRAFT;
        Boolean regRequired = request.getRegistrationRequired() != null ? request.getRegistrationRequired() : false;

        return Event.builder()
                .id(UUID.randomUUID())
                .title(trim(request.getTitle()))
                .slug(trim(request.getSlug()))
                .description(trim(request.getDescription()))
                .summary(trim(request.getSummary()))
                .councilId(request.getCouncilId())
                .venue(trim(request.getVenue()))
                .eventType(request.getEventType())
                .status(status)
                .registrationRequired(regRequired)
                .registrationStart(request.getRegistrationStart())
                .registrationEnd(request.getRegistrationEnd())
                .capacity(request.getCapacity())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .bannerUrl(trim(request.getBannerUrl()))
                .contactEmail(trim(request.getContactEmail()))
                .contactNumber(trim(request.getContactNumber()))
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void updateEntityFromRequest(Event event, UpdateEventRequest request) {
        if (event == null || request == null) {
            return;
        }

        event.setTitle(trim(request.getTitle()));
        event.setSlug(trim(request.getSlug()));
        event.setDescription(trim(request.getDescription()));
        event.setSummary(trim(request.getSummary()));
        event.setVenue(trim(request.getVenue()));
        event.setEventType(request.getEventType());
        if (request.getRegistrationRequired() != null) {
            event.setRegistrationRequired(request.getRegistrationRequired());
        }
        event.setRegistrationStart(request.getRegistrationStart());
        event.setRegistrationEnd(request.getRegistrationEnd());
        event.setCapacity(request.getCapacity());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setBannerUrl(trim(request.getBannerUrl()));
        event.setContactEmail(trim(request.getContactEmail()));
        event.setContactNumber(trim(request.getContactNumber()));
        event.setUpdatedAt(Instant.now());
    }

    public EventResponse toResponse(Event event) {
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
                .createdAt(event.getCreatedAt() != null ? java.time.LocalDateTime.ofInstant(event.getCreatedAt(), java.time.ZoneId.systemDefault()) : null)
                .updatedAt(event.getUpdatedAt() != null ? java.time.LocalDateTime.ofInstant(event.getUpdatedAt(), java.time.ZoneId.systemDefault()) : null)
                .build();
    }

    public List<EventResponse> toResponseList(List<Event> events) {
        if (events == null) {
            return Collections.emptyList();
        }

        return events.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private String trim(String value) {
        return value != null ? value.trim() : null;
    }
}
