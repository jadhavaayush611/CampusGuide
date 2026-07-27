package com.campusguide.campus.event.controller;

import com.campusguide.campus.event.dto.EventResponse;
import com.campusguide.campus.event.service.EventRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventRegistrationController {

    private final EventRegistrationService registrationService;

    @PostMapping("/{id}/register")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EventResponse> registerForEvent(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        EventResponse response = registrationService.registerForEvent(id, userDetails);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/register")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EventResponse> cancelRegistration(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        EventResponse response = registrationService.cancelRegistration(id, userDetails);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/is-registered")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> isUserRegistered(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        boolean isRegistered = registrationService.isUserRegistered(id, userDetails);
        return ResponseEntity.ok(isRegistered);
    }

    @GetMapping("/{id}/registered-users")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COUNCIL_ADMIN')")
    public ResponseEntity<List<String>> getRegisteredUsers(@PathVariable UUID id) {
        List<String> userIds = registrationService.getRegisteredUsers(id);
        return ResponseEntity.ok(userIds);
    }
}
