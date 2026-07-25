package com.campusguide.academic.controller;

import com.campusguide.academic.dto.AcademicDashboardResponse;
import com.campusguide.academic.dto.AcademicProgressResponse;
import com.campusguide.academic.dto.RecommendedSemesterResponse;
import com.campusguide.academic.service.AcademicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/academic")
@RequiredArgsConstructor
@Validated
public class AcademicController {

    private final AcademicService academicService;

    @GetMapping("/dashboard")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AcademicDashboardResponse> getDashboard(
            @AuthenticationPrincipal UserDetails userDetails) {
        AcademicDashboardResponse response = academicService.getDashboard(userDetails);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/progress")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AcademicProgressResponse> getProgress(
            @AuthenticationPrincipal UserDetails userDetails) {
        AcademicProgressResponse response = academicService.getProgress(userDetails);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/recommended-semester")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RecommendedSemesterResponse> getRecommendedSemester(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Integer semesterNumber) {
        RecommendedSemesterResponse response = academicService.getRecommendedSemester(userDetails, semesterNumber);
        return ResponseEntity.ok(response);
    }
}
