package com.campusguide.personal.planner.controller;

import com.campusguide.personal.planner.dto.CreateStudyGoalRequest;
import com.campusguide.personal.planner.dto.StudyGoalResponse;
import com.campusguide.personal.planner.dto.UpdateStudyGoalRequest;
import com.campusguide.personal.planner.service.StudyGoalService;
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

@RestController
@RequestMapping("/api/v1/planner/goals")
@RequiredArgsConstructor
@Validated
public class StudyGoalController {

    private final StudyGoalService studyGoalService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StudyGoalResponse> createGoal(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateStudyGoalRequest request) {
        StudyGoalResponse response = studyGoalService.createGoal(userDetails, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<StudyGoalResponse>> getAllGoals(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<StudyGoalResponse> response = studyGoalService.getGoals(userDetails);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StudyGoalResponse> getGoalById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        StudyGoalResponse response = studyGoalService.getGoalById(userDetails, id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StudyGoalResponse> updateGoal(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStudyGoalRequest request) {
        StudyGoalResponse response = studyGoalService.updateGoal(userDetails, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteGoal(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        studyGoalService.deleteGoal(userDetails, id);
        return ResponseEntity.noContent().build();
    }
}
