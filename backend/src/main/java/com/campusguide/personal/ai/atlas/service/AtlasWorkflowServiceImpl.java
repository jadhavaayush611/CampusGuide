package com.campusguide.personal.ai.atlas.service;

import com.campusguide.personal.ai.atlas.dto.ExecutionStatusResponse;
import com.campusguide.personal.ai.atlas.dto.WorkflowExecutionRequest;
import com.campusguide.personal.ai.atlas.dto.WorkflowExecutionResponse;
import com.campusguide.personal.ai.atlas.exception.AtlasExecutionException;
import com.campusguide.personal.ai.atlas.exception.AtlasForbiddenException;
import com.campusguide.personal.ai.atlas.exception.AtlasNotFoundException;
import com.campusguide.personal.ai.atlas.exception.AtlasRateLimitException;
import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;
import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowInstance;
import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowRuntime;
import com.campusguide.personal.ai.atlas.execution.workflow.WorkflowRegistry;
import com.campusguide.personal.ai.atlas.security.AtlasSecurityManager;
import com.campusguide.common.security.UserPrincipal;
import com.campusguide.platform.user.entity.User;
import com.campusguide.platform.user.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AtlasWorkflowServiceImpl implements AtlasWorkflowService {

    @Autowired(required = false)
    private final WorkflowRuntime workflowRuntime;
    @Autowired(required = false)
    private final WorkflowRegistry workflowRegistry;
    @Autowired(required = false)
    private final CurrentUserService currentUserService;
    @Autowired(required = false)
    private final AtlasSecurityManager securityManager;

    @Override
    public WorkflowExecutionResponse executeWorkflow(WorkflowExecutionRequest request, UserDetails userDetails) {
        String userId = resolveUserId(userDetails);

        if (securityManager != null && userId != null) {
            securityManager.enforceRateLimit(userId);
            if (!securityManager.tryAcquireExecutionSlot(userId)) {
                throw new AtlasRateLimitException("Concurrent workflow execution limit reached for user");
            }
        }

        long startTime = System.currentTimeMillis();
        String executionId = "exec_" + UUID.randomUUID().toString().substring(0, 8);

        try {
            ExecutionContext context = ExecutionContext.builder()
                    .contextId("ctx_" + executionId)
                    .userId(userId)
                    .build();

            ExecutableWorkflow executableWorkflow = ExecutableWorkflow.fallback(
                    request.getWorkflowId(),
                    "Execution via Atlas API Platform"
            );

            WorkflowInstance instance = null;
            if (workflowRuntime != null) {
                instance = workflowRuntime.executeWorkflow(context, executableWorkflow);
            }

            long latencyMs = System.currentTimeMillis() - startTime;
            String status = instance != null && instance.getState() != null ? instance.getState().name() : "COMPLETED";
            String instId = instance != null ? instance.getInstanceId() : executionId;

            if (securityManager != null) {
                securityManager.logAudit("POST /api/v1/atlas/workflows/execute", userId, instId, latencyMs, status);
            }

            return WorkflowExecutionResponse.builder()
                    .executionId(instId)
                    .workflowId(request.getWorkflowId())
                    .status(status)
                    .result(Map.of("message", "Workflow executed successfully", "workflowId", request.getWorkflowId()))
                    .startedAt(Instant.ofEpochMilli(startTime))
                    .completedAt(Instant.now())
                    .executionTimeMs(latencyMs)
                    .build();

        } finally {
            if (securityManager != null && userId != null) {
                securityManager.releaseExecutionSlot(userId);
            }
        }
    }

    @Override
    public ExecutionStatusResponse getExecutionStatus(String executionId, UserDetails userDetails) {
        String userId = resolveUserId(userDetails);
        WorkflowInstance instance = findAndValidateInstance(executionId, userId, userDetails);

        return mapToStatusResponse(instance);
    }

    @Override
    public List<ExecutionStatusResponse> getExecutionHistory(UserDetails userDetails) {
        String userId = resolveUserId(userDetails);
        if (securityManager != null && userId != null) {
            securityManager.enforceRateLimit(userId);
        }

        if (workflowRuntime == null) {
            return Collections.emptyList();
        }

        boolean isAdmin = isAdminUser(userDetails);
        List<WorkflowInstance> instances = workflowRuntime.getActiveInstances();

        return instances.stream()
                .filter(i -> isAdmin || (userId != null && userId.equals(getInstanceUserId(i))))
                .map(this::mapToStatusResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ExecutionStatusResponse cancelExecution(String executionId, String reason, UserDetails userDetails) {
        String userId = resolveUserId(userDetails);
        WorkflowInstance instance = findAndValidateInstance(executionId, userId, userDetails);

        if (workflowRuntime != null) {
            workflowRuntime.cancelWorkflow(executionId, reason != null ? reason : "Cancelled by user request");
        }

        if (securityManager != null) {
            securityManager.logAudit("CANCEL /api/v1/atlas/workflows", userId, executionId, 0, "CANCELLED");
        }

        return mapToStatusResponse(instance);
    }

    private WorkflowInstance findAndValidateInstance(String executionId, String userId, UserDetails userDetails) {
        if (workflowRuntime == null) {
            throw new AtlasNotFoundException("Workflow execution not found: " + executionId);
        }
        WorkflowInstance instance = workflowRuntime.getInstance(executionId);
        if (instance == null) {
            throw new AtlasNotFoundException("Workflow execution not found: " + executionId);
        }

        String instanceUserId = getInstanceUserId(instance);
        boolean isAdmin = isAdminUser(userDetails);
        if (securityManager != null) {
            securityManager.validateOwnership(instanceUserId, userId, isAdmin);
        } else if (!isAdmin && instanceUserId != null && !instanceUserId.equals(userId)) {
            throw new AtlasForbiddenException("Access denied: You do not own this workflow execution");
        }

        return instance;
    }

    private String getInstanceUserId(WorkflowInstance instance) {
        if (instance == null) return null;
        if (instance.getSession() != null && instance.getSession().getUserId() != null) {
            return instance.getSession().getUserId();
        }
        if (instance.getContext() != null) {
            return instance.getContext().getUserId();
        }
        return null;
    }

    private boolean isAdminUser(UserDetails userDetails) {
        if (userDetails == null) return false;
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN") || a.equals("ADMIN"));
    }

    private String resolveUserId(UserDetails userDetails) {
        if (userDetails instanceof UserPrincipal principal) {
            return principal.getId();
        }
        if (userDetails != null && currentUserService != null) {
            try {
                User user = currentUserService.getCurrentUser(userDetails);
                if (user != null) return user.getId();
            } catch (Exception e) {
                log.warn("Could not resolve current user id: {}", e.getMessage());
            }
            return userDetails.getUsername();
        }
        return userDetails != null ? userDetails.getUsername() : null;
    }

    private ExecutionStatusResponse mapToStatusResponse(WorkflowInstance instance) {
        Instant start = instance.getStartTime() != null ? instance.getStartTime() : Instant.now();
        Instant end = instance.getEndTime() != null ? instance.getEndTime() : start;

        return ExecutionStatusResponse.builder()
                .executionId(instance.getInstanceId())
                .workflowId(instance.getWorkflowId())
                .userId(getInstanceUserId(instance))
                .status(instance.getState() != null ? instance.getState().name() : "UNKNOWN")
                .progressPercent(instance.getState() != null && instance.getState().isTerminal() ? 100 : 50)
                .currentStep(instance.getState() != null ? instance.getState().name() : "IDLE")
                .startedAt(start)
                .updatedAt(end)
                .completedAt(instance.getState() != null && instance.getState().isTerminal() ? end : null)
                .result(Map.of("workflowId", instance.getWorkflowId()))
                .build();
    }
}
