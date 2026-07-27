package com.campusguide.personal.calendar.controller;

import com.campusguide.personal.calendar.dto.CalendarEntryResponse;
import com.campusguide.personal.calendar.dto.CreateCalendarEntryRequest;
import com.campusguide.personal.calendar.dto.UpdateCalendarEntryRequest;
import com.campusguide.personal.calendar.service.CalendarEntryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/calendar")
@RequiredArgsConstructor
@Validated
public class CalendarEntryController {

    private final CalendarEntryService calendarEntryService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CalendarEntryResponse> createEntry(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateCalendarEntryRequest request) {
        CalendarEntryResponse response = calendarEntryService.createEntry(userDetails, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CalendarEntryResponse>> getAllEntries(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<CalendarEntryResponse> response = calendarEntryService.getAllEntries(userDetails);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/range")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CalendarEntryResponse>> getEntriesInRange(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        List<CalendarEntryResponse> response = calendarEntryService.getEntriesInRange(userDetails, from, to);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CalendarEntryResponse> getEntryById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        CalendarEntryResponse response = calendarEntryService.getEntryById(userDetails, id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CalendarEntryResponse> updateEntry(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCalendarEntryRequest request) {
        CalendarEntryResponse response = calendarEntryService.updateEntry(userDetails, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteEntry(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        calendarEntryService.deleteEntry(userDetails, id);
        return ResponseEntity.noContent().build();
    }
}
