package com.campusguide.platform.analytics.controller;

import com.campusguide.platform.analytics.dto.response.DashboardSummaryResponse;
import com.campusguide.platform.analytics.dto.response.ModuleStatisticsResponse;
import com.campusguide.platform.analytics.service.interfaces.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary() {
        return ResponseEntity.ok(analyticsService.getDashboardSummary());
    }

    @GetMapping("/users")
    public ResponseEntity<ModuleStatisticsResponse> getUserStatistics() {
        return ResponseEntity.ok(analyticsService.getUserStatistics());
    }

    @GetMapping("/events")
    public ResponseEntity<ModuleStatisticsResponse> getEventStatistics() {
        return ResponseEntity.ok(analyticsService.getEventStatistics());
    }

    @GetMapping("/communities")
    public ResponseEntity<ModuleStatisticsResponse> getCommunityStatistics() {
        return ResponseEntity.ok(analyticsService.getCommunityStatistics());
    }
}
