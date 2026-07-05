package com.campusguide.modules.event.controller;

import com.campusguide.modules.event.dto.EventResponse;
import com.campusguide.modules.event.service.EventRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventRegistrationController {

    private final EventRegistrationService eventRegistrationService;

    @PostMapping("/{eventId}/register")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EventResponse> registerForEvent(
            @PathVariable String eventId,
            @AuthenticationPrincipal UserDetails userDetails) {
        EventResponse response = eventRegistrationService.registerForEvent(eventId, userDetails);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{eventId}/register")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EventResponse> cancelRegistration(
            @PathVariable String eventId,
            @AuthenticationPrincipal UserDetails userDetails) {
        EventResponse response = eventRegistrationService.cancelRegistration(eventId, userDetails);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{eventId}/registration-status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Boolean>> getRegistrationStatus(
            @PathVariable String eventId,
            @AuthenticationPrincipal UserDetails userDetails) {
        boolean registered = eventRegistrationService.isUserRegistered(eventId, userDetails);
        return ResponseEntity.ok(Map.of("registered", registered));
    }

    @GetMapping("/{eventId}/registrations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<String>> getRegisteredUsers(
            @PathVariable String eventId) {
        List<String> registeredUsers = eventRegistrationService.getRegisteredUsers(eventId);
        return ResponseEntity.ok(registeredUsers);
    }
}
