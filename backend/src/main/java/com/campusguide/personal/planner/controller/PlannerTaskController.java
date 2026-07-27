package com.campusguide.personal.planner.controller;

import com.campusguide.personal.planner.dto.CreatePlannerTaskRequest;
import com.campusguide.personal.planner.dto.PlannerTaskResponse;
import com.campusguide.personal.planner.dto.UpdatePlannerTaskRequest;
import com.campusguide.personal.planner.dto.UpdateTaskStatusRequest;
import com.campusguide.personal.planner.service.PlannerTaskService;
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
@RequestMapping("/api/v1/planner")
@RequiredArgsConstructor
@Validated
public class PlannerTaskController {

    private final PlannerTaskService plannerTaskService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PlannerTaskResponse> createTask(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreatePlannerTaskRequest request) {
        PlannerTaskResponse response = plannerTaskService.createTask(userDetails, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PlannerTaskResponse>> getAllTasks(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<PlannerTaskResponse> response = plannerTaskService.getAllTasks(userDetails);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PlannerTaskResponse> getTaskById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        PlannerTaskResponse response = plannerTaskService.getTaskById(userDetails, id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PlannerTaskResponse> updateTask(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePlannerTaskRequest request) {
        PlannerTaskResponse response = plannerTaskService.updateTask(userDetails, id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PlannerTaskResponse> updateTaskStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskStatusRequest request) {
        PlannerTaskResponse response = plannerTaskService.updateTaskStatus(userDetails, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteTask(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        plannerTaskService.deleteTask(userDetails, id);
        return ResponseEntity.noContent().build();
    }
}
