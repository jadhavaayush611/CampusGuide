package com.campusguide.personal.notification.controller;

import com.campusguide.personal.notification.dto.CreateScheduledNotificationRequest;
import com.campusguide.personal.notification.dto.ScheduledNotificationResponse;
import com.campusguide.personal.notification.dto.UpdateNotificationStatusRequest;
import com.campusguide.personal.notification.dto.UpdateScheduledNotificationRequest;
import com.campusguide.personal.notification.service.ScheduledNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller for managing scheduled notifications and future reminders.
 * Manages creation of scheduled alerts linked to tasks, events, and calendar entries, as well as pending triggers and status updates.
 * Unambiguously distinct from NotificationController (/api/v1/notifications).
 */
@RestController
@RequestMapping("/api/v1/scheduled-notifications")
@RequiredArgsConstructor
@Validated
public class ScheduledNotificationController {

    private final ScheduledNotificationService scheduledNotificationService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ScheduledNotificationResponse> createNotification(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateScheduledNotificationRequest request) {
        ScheduledNotificationResponse response = scheduledNotificationService.createNotification(userDetails, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ScheduledNotificationResponse>> getAllNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<ScheduledNotificationResponse> response = scheduledNotificationService.getAllNotifications(userDetails);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ScheduledNotificationResponse>> getPendingNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<ScheduledNotificationResponse> response = scheduledNotificationService.getPendingNotifications(userDetails);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ScheduledNotificationResponse> getNotificationById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        ScheduledNotificationResponse response = scheduledNotificationService.getNotificationById(userDetails, id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ScheduledNotificationResponse> updateNotificationStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateNotificationStatusRequest request) {
        ScheduledNotificationResponse response = scheduledNotificationService.updateNotificationStatus(userDetails, id, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ScheduledNotificationResponse> updateNotification(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateScheduledNotificationRequest request) {
        ScheduledNotificationResponse response = scheduledNotificationService.updateNotification(userDetails, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteNotification(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        scheduledNotificationService.deleteNotification(userDetails, id);
        return ResponseEntity.noContent().build();
    }
}
