package com.campusguide.modules.semester.controller;

import com.campusguide.modules.semester.dto.CreateSemesterPlanRequest;
import com.campusguide.modules.semester.dto.SemesterPlanResponse;
import com.campusguide.modules.semester.dto.UpdateSemesterPlanRequest;
import com.campusguide.modules.semester.service.SemesterPlanService;
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

@RestController
@RequestMapping("/api/semester-plans")
@RequiredArgsConstructor
@Validated
public class SemesterPlanController {

    private final SemesterPlanService semesterPlanService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SemesterPlanResponse> createSemesterPlan(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateSemesterPlanRequest request) {
        SemesterPlanResponse response = semesterPlanService.createSemesterPlan(userDetails, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{planId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SemesterPlanResponse> updateSemesterPlan(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String planId,
            @Valid @RequestBody UpdateSemesterPlanRequest request) {
        SemesterPlanResponse response = semesterPlanService.updateSemesterPlan(userDetails, planId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SemesterPlanResponse>> getMyPlans(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<SemesterPlanResponse> response = semesterPlanService.getMyPlans(userDetails);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{planId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SemesterPlanResponse> getSemesterPlan(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String planId) {
        SemesterPlanResponse response = semesterPlanService.getSemesterPlan(userDetails, planId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{planId}/add/{courseId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SemesterPlanResponse> addCourse(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String planId,
            @PathVariable String courseId) {
        SemesterPlanResponse response = semesterPlanService.addCourse(userDetails, planId, courseId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{planId}/remove/{courseId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SemesterPlanResponse> removeCourse(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String planId,
            @PathVariable String courseId) {
        SemesterPlanResponse response = semesterPlanService.removeCourse(userDetails, planId, courseId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{planId}/finalize")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SemesterPlanResponse> finalizeSemesterPlan(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String planId) {
        SemesterPlanResponse response = semesterPlanService.finalizeSemesterPlan(userDetails, planId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<SemesterPlanResponse>> getPlansByStudent(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String studentId) {
        List<SemesterPlanResponse> response = semesterPlanService.getPlansByStudent(userDetails, studentId);
        return ResponseEntity.ok(response);
    }
}
