package com.campusguide.personal.ai.atlas.execution.runtime.security;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Entry point for runtime authorization validation before every tool invocation.
 * Maintains security audit trail without exposing sensitive data.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExecutionSecurityManager {

    private final ExecutionPermissionValidator permissionValidator;
    private final List<SecurityAudit> auditTrail = Collections.synchronizedList(new ArrayList<>());

    public boolean authorizeExecution(ExecutionContext context, ExecutionUnit unit, String workflowId) {
        if (unit == null) {
            log.warn("Null ExecutionUnit passed for security authorization");
            return false;
        }

        String capability = unit.getTargetCapability();
        String userId = context != null ? context.getUserId() : "unknown";

        boolean authorized = permissionValidator.validatePermission(context, unit);
        String action = authorized ? "ALLOWED" : "DENIED";
        String reason = authorized ? "Security authorization check passed" : "Permission check failed for capability: " + capability;

        SecurityAudit audit = SecurityAudit.builder()
                .workflowId(workflowId)
                .unitId(unit.getUnitId())
                .capability(capability)
                .userId(userId)
                .action(action)
                .reason(reason)
                .build();

        auditTrail.add(audit);

        if (!authorized) {
            log.warn("Security authorization DENIED for workflow {} unit {} (capability: {})", workflowId, unit.getUnitId(), capability);
        } else {
            log.debug("Security authorization ALLOWED for workflow {} unit {}", workflowId, unit.getUnitId());
        }

        return authorized;
    }

    public List<SecurityAudit> getAuditTrail() {
        return new ArrayList<>(auditTrail);
    }
}
