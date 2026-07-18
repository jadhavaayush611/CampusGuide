package com.campusguide.modules.progress.controller;

import com.campusguide.modules.progress.dto.AdminUpdateStudentProgressRequest;
import com.campusguide.modules.progress.dto.CreateStudentProgressRequest;
import com.campusguide.modules.progress.dto.StudentProgressResponse;
import com.campusguide.modules.progress.dto.UpdateStudentProgressRequest;
import com.campusguide.modules.progress.service.StudentProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
@Validated
public class StudentProgressController {

    private final StudentProgressService studentProgressService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StudentProgressResponse> createProgress(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateStudentProgressRequest request) {
        StudentProgressResponse response = studentProgressService.createProgress(userDetails, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StudentProgressResponse> updateProgress(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateStudentProgressRequest request) {
        StudentProgressResponse response = studentProgressService.updateProgress(userDetails, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/admin")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<StudentProgressResponse> adminUpdateProgress(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AdminUpdateStudentProgressRequest request) {
        StudentProgressResponse response = studentProgressService.adminUpdateProgress(userDetails, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StudentProgressResponse> getProgress(
            @AuthenticationPrincipal UserDetails userDetails) {
        StudentProgressResponse response = studentProgressService.getProgress(userDetails);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/complete/{courseId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StudentProgressResponse> markCourseCompleted(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String courseId,
            @RequestParam(required = false) String studentId) {
        StudentProgressResponse response = studentProgressService.markCourseCompleted(userDetails, courseId, studentId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/remove/{courseId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StudentProgressResponse> removeCompletedCourse(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String courseId,
            @RequestParam(required = false) String studentId) {
        StudentProgressResponse response = studentProgressService.removeCompletedCourse(userDetails, courseId, studentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<StudentProgressResponse> getProgressByStudent(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String studentId) {
        StudentProgressResponse response = studentProgressService.getProgressByStudent(userDetails, studentId);
        return ResponseEntity.ok(response);
    }
}
