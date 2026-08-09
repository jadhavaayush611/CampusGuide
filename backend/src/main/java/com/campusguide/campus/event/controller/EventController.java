package com.campusguide.campus.event.controller;

import com.campusguide.campus.event.dto.CreateEventRequest;
import com.campusguide.campus.event.dto.EventResponse;
import com.campusguide.campus.event.dto.UpdateEventRequest;
import com.campusguide.campus.event.dto.UpdateEventStatusRequest;
import com.campusguide.campus.event.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COUNCIL_ADMIN')")
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody CreateEventRequest request) {
        EventResponse response = eventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EventResponse>> getPublicEvents() {
        List<EventResponse> response = eventService.getPublicUpcomingEvents();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/upcoming")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EventResponse>> getUpcomingEvents() {
        List<EventResponse> response = eventService.getPublicUpcomingEvents();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EventResponse> getEventById(@PathVariable UUID id) {
        EventResponse response = eventService.getEventById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/slug/{slug}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EventResponse> getEventBySlug(@PathVariable String slug) {
        EventResponse response = eventService.getEventBySlug(slug);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/council/{councilId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EventResponse>> getEventsByCouncil(@PathVariable UUID councilId) {
        List<EventResponse> response = eventService.getEventsByCouncil(councilId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COUNCIL_ADMIN')")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEventRequest request) {
        EventResponse response = eventService.updateEvent(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COUNCIL_ADMIN')")
    public ResponseEntity<EventResponse> updateEventStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEventStatusRequest request) {
        EventResponse response = eventService.updateEventStatus(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COUNCIL_ADMIN')")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
