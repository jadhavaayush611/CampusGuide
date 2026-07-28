package com.campusguide.campus.academic.roadmap.controller;

import com.campusguide.campus.academic.roadmap.dto.CreateRoadmapRequest;
import com.campusguide.campus.academic.roadmap.dto.RoadmapResponse;
import com.campusguide.campus.academic.roadmap.dto.RoadmapSummaryResponse;
import com.campusguide.campus.academic.roadmap.dto.UpdateRoadmapRequest;
import com.campusguide.campus.academic.roadmap.service.RoadmapService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roadmaps")
@RequiredArgsConstructor
public class RoadmapController {

    private final RoadmapService roadmapService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RoadmapResponse> createRoadmap(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateRoadmapRequest request) {
        RoadmapResponse response = roadmapService.createRoadmap(userDetails, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{roadmapId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RoadmapResponse> updateRoadmap(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String roadmapId,
            @Valid @RequestBody UpdateRoadmapRequest request) {
        RoadmapResponse response = roadmapService.updateRoadmap(userDetails, roadmapId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{roadmapId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteRoadmap(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String roadmapId) {
        roadmapService.deleteRoadmap(userDetails, roadmapId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RoadmapSummaryResponse>> getAllRoadmaps() {
        List<RoadmapSummaryResponse> response = roadmapService.getAllRoadmaps();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/creator/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RoadmapSummaryResponse>> getRoadmapsByCreator(@PathVariable String userId) {
        List<RoadmapSummaryResponse> response = roadmapService.getRoadmapsByCreator(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/degree/{degreeProgram}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RoadmapSummaryResponse>> getRoadmapsByDegree(@PathVariable String degreeProgram) {
        List<RoadmapSummaryResponse> response = roadmapService.getRoadmapsByDegree(degreeProgram);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/department/{department}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RoadmapSummaryResponse>> getRoadmapsByDepartment(@PathVariable String department) {
        List<RoadmapSummaryResponse> response = roadmapService.getRoadmapsByDepartment(department);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{roadmapId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RoadmapResponse> getRoadmapById(@PathVariable String roadmapId) {
        RoadmapResponse response = roadmapService.getRoadmapById(roadmapId);
        return ResponseEntity.ok(response);
    }
}
