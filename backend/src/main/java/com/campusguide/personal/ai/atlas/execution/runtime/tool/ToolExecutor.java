package com.campusguide.personal.ai.atlas.execution.runtime.tool;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionRetryPolicy;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionUnit;
import com.campusguide.personal.ai.atlas.execution.runtime.retry.RetryDecision;
import com.campusguide.personal.ai.atlas.execution.runtime.retry.RetryEngine;
import com.campusguide.personal.ai.atlas.execution.runtime.retry.RetryPolicy;
import com.campusguide.personal.ai.atlas.execution.runtime.retry.RetryStrategy;
import com.campusguide.personal.ai.atlas.execution.runtime.security.ExecutionSecurityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Provider-independent tool execution engine. Validates runtime authorization,
 * resolves tool adapters, executes tool calls, and handles retries.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ToolExecutor {

    private final ToolRegistry toolRegistry;
    private final ExecutionSecurityManager securityManager;
    private final RetryEngine retryEngine;

    public ToolResult executeUnit(ExecutionContext context, ExecutionUnit unit, String workflowId) {
        if (unit == null) {
            return ToolResult.failure("inv_null", "unit_null", "Null execution unit provided", 0L);
        }

        String capability = unit.getTargetCapability() != null ? unit.getTargetCapability() : "default.action";
        String invocationId = "inv_" + UUID.randomUUID().toString().substring(0, 8);

        // Security check
        boolean authorized = securityManager.authorizeExecution(context, unit, workflowId);
        if (!authorized) {
            return ToolResult.denied(invocationId, unit.getUnitId(), "Security authorization check failed for capability: " + capability);
        }

        // Approval check
        if (unit.isApprovalRequired()) {
            return ToolResult.waitingForApproval(invocationId, unit.getUnitId(), "Execution unit requires manual human approval");
        }

        ToolAdapter adapter = toolRegistry.resolveAdapter(capability);
        ToolInvocation invocation = ToolInvocation.builder()
                .invocationId(invocationId)
                .workflowId(workflowId)
                .contextId(context != null ? context.getContextId() : null)
                .unitId(unit.getUnitId())
                .capability(capability)
                .payload(unit.getPayload())
                .timeoutSeconds(unit.getTimeoutSeconds() > 0 ? unit.getTimeoutSeconds() : 60L)
                .securityContext(context != null ? context.getSecurityContext() : null)
                .userId(context != null ? context.getUserId() : "system")
                .build();

        RetryPolicy retryPolicy = mapRetryPolicy(unit.getRetryPolicy());
        int attempt = 0;
        ToolResult result = null;

        while (true) {
            long start = System.currentTimeMillis();
            try {
                result = adapter.execute(invocation);
                if (result != null && result.getStatus() == ToolExecutionStatus.SUCCESS) {
                    retryEngine.resetFailureCount(unit.getUnitId());
                    return result;
                }
            } catch (Exception e) {
                long duration = System.currentTimeMillis() - start;
                log.error("Exception during tool execution for unit {}: {}", unit.getUnitId(), e.getMessage());
                result = ToolResult.failure(invocationId, unit.getUnitId(), e.getMessage(), duration);
            }

            // Failure handling & retry evaluation
            attempt++;
            RetryDecision decision = retryEngine.evaluateRetry(unit.getUnitId(), attempt, retryPolicy, null);
            if (decision.isShouldRetry()) {
                log.info("Retrying execution unit {} (attempt {} after {} ms delay)", unit.getUnitId(), decision.getAttemptNumber(), decision.getBackoffMs());
                if (decision.getBackoffMs() > 0) {
                    try {
                        Thread.sleep(Math.min(decision.getBackoffMs(), 5000L)); // cap sleep during tests/runtime
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return ToolResult.failure(invocationId, unit.getUnitId(), "Interrupted during retry backoff", 0L);
                    }
                }
            } else {
                break;
            }
        }

        return result != null ? result : ToolResult.failure(invocationId, unit.getUnitId(), "Tool execution failed after retries", 0L);
    }

    private RetryPolicy mapRetryPolicy(ExecutionRetryPolicy execRetryPolicy) {
        if (execRetryPolicy == null || execRetryPolicy.getMaxRetries() <= 0) {
            return RetryPolicy.noRetry();
        }
        return RetryPolicy.builder()
                .maxRetries(execRetryPolicy.getMaxRetries())
                .initialBackoffMs(execRetryPolicy.getInitialBackoffMs())
                .maxBackoffMs(execRetryPolicy.getMaxBackoffMs())
                .backoffMultiplier(execRetryPolicy.getBackoffMultiplier())
                .strategy(RetryStrategy.EXPONENTIAL_BACKOFF)
                .build();
    }
}
