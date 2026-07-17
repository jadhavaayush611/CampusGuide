package com.campusguide.modules.notification.controller;

import com.campusguide.modules.notification.dto.response.NotificationResponse;
import com.campusguide.modules.notification.service.interfaces.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Validated
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<NotificationResponse>> listNotifications(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<NotificationResponse> response = notificationService.listNotifications(userDetails, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/unread")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<NotificationResponse>> listUnreadNotifications(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<NotificationResponse> response = notificationService.listUnreadNotifications(userDetails, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/unread/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Long>> countUnreadNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {
        long count = notificationService.countUnreadNotifications(userDetails);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificationResponse> markAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id) {
        NotificationResponse response = notificationService.markAsRead(userDetails, id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails) {
        notificationService.markAllAsRead(userDetails);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteNotification(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id) {
        notificationService.deleteNotification(userDetails, id);
        return ResponseEntity.noContent().build();
    }
}
