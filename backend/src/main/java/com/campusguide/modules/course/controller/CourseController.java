package com.campusguide.modules.course.controller;

import com.campusguide.modules.course.dto.CreateCourseRequest;
import com.campusguide.modules.course.dto.CourseResponse;
import com.campusguide.modules.course.dto.CourseSummaryResponse;
import com.campusguide.modules.course.dto.UpdateCourseRequest;
import com.campusguide.modules.course.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        CourseResponse response = courseService.createCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{courseId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<CourseResponse> updateCourse(
            @PathVariable String courseId,
            @Valid @RequestBody UpdateCourseRequest request) {
        CourseResponse response = courseService.updateCourse(courseId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteCourse(@PathVariable String courseId) {
        courseService.deleteCourse(courseId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CourseSummaryResponse>> getAllCourses() {
        List<CourseSummaryResponse> response = courseService.getAllCourses();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/electives")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CourseSummaryResponse>> getElectives() {
        List<CourseSummaryResponse> response = courseService.getElectives();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/mandatory")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CourseSummaryResponse>> getMandatoryCourses() {
        List<CourseSummaryResponse> response = courseService.getMandatoryCourses();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/department/{department}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CourseSummaryResponse>> getByDepartment(@PathVariable String department) {
        List<CourseSummaryResponse> response = courseService.getByDepartment(department);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/semester/{semester}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CourseSummaryResponse>> getBySemester(@PathVariable Integer semester) {
        List<CourseSummaryResponse> response = courseService.getBySemester(semester);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{courseId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CourseResponse> getCourseById(@PathVariable String courseId) {
        CourseResponse response = courseService.getCourseById(courseId);
        return ResponseEntity.ok(response);
    }
}
