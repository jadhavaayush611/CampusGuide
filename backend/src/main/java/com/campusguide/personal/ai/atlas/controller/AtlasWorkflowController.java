package com.campusguide.personal.ai.atlas.controller;

import com.campusguide.personal.ai.atlas.dto.ExecutionStatusResponse;
import com.campusguide.personal.ai.atlas.dto.WorkflowExecutionRequest;
import com.campusguide.personal.ai.atlas.dto.WorkflowExecutionResponse;
import com.campusguide.personal.ai.atlas.service.AtlasWorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/atlas/workflows")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AtlasWorkflowController {

    private final AtlasWorkflowService workflowService;

    @PostMapping({"/execute", "/executions"})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WorkflowExecutionResponse> executeWorkflow(
            @Valid @RequestBody WorkflowExecutionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Received POST /api/v1/atlas/workflows/execute request for workflow: {}", request.getWorkflowId());
        WorkflowExecutionResponse response = workflowService.executeWorkflow(request, userDetails);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping({"/executions/{executionId}", "/{executionId}/status"})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ExecutionStatusResponse> getExecutionStatus(
            @PathVariable("executionId") String executionId,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Received GET status request for execution: {}", executionId);
        ExecutionStatusResponse response = workflowService.getExecutionStatus(executionId, userDetails);
        return ResponseEntity.ok(response);
    }

    @GetMapping({"/executions", "/history"})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ExecutionStatusResponse>> getExecutionHistory(
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Received GET /api/v1/atlas/workflows/executions history request");
        List<ExecutionStatusResponse> response = workflowService.getExecutionHistory(userDetails);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/executions/{executionId}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ExecutionStatusResponse> cancelExecution(
            @PathVariable("executionId") String executionId,
            @RequestParam(value = "reason", required = false, defaultValue = "User requested cancellation") String reason,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Received POST cancel request for execution: {}", executionId);
        ExecutionStatusResponse response = workflowService.cancelExecution(executionId, reason, userDetails);
        return ResponseEntity.ok(response);
    }
}
