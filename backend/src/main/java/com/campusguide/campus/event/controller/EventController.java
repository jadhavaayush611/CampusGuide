package com.campusguide.campus.event.controller;

import com.campusguide.campus.event.dto.CreateEventRequest;
import com.campusguide.campus.event.dto.EventResponse;
import com.campusguide.campus.event.dto.EventSummaryResponse;
import com.campusguide.campus.event.dto.UpdateEventRequest;
import com.campusguide.campus.event.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EventResponse> createEvent(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateEventRequest request) {
        EventResponse response = eventService.createEvent(userDetails, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{eventId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EventResponse> updateEvent(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String eventId,
            @Valid @RequestBody UpdateEventRequest request) {
        EventResponse response = eventService.updateEvent(userDetails, eventId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{eventId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteEvent(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String eventId) {
        eventService.deleteEvent(userDetails, eventId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EventSummaryResponse>> getAllActiveEvents() {
        List<EventSummaryResponse> response = eventService.getAllActiveEvents();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/upcoming")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EventSummaryResponse>> getUpcomingEvents() {
        List<EventSummaryResponse> response = eventService.getUpcomingEvents();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/past")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EventSummaryResponse>> getPastEvents() {
        List<EventSummaryResponse> response = eventService.getPastEvents();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/organizer/{organizerId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EventSummaryResponse>> getEventsByOrganizer(@PathVariable String organizerId) {
        List<EventSummaryResponse> response = eventService.getEventsByOrganizer(organizerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EventSummaryResponse>> searchEvents(@RequestParam String query) {
        List<EventSummaryResponse> response = eventService.searchEvents(query);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/range")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EventSummaryResponse>> getEventsBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<EventSummaryResponse> response = eventService.getEventsBetween(start, end);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{eventId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EventResponse> getEventById(@PathVariable String eventId) {
        EventResponse response = eventService.getEventById(eventId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/council/{councilId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EventSummaryResponse>> getEventsByCouncil(@PathVariable String councilId) {
        List<EventSummaryResponse> response = eventService.getEventsByCouncil(councilId);
        return ResponseEntity.ok(response);
    }
}
