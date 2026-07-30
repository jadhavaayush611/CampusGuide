package com.campusguide.personal.ai.atlas.execution.runtime.security;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.context.SecurityContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionUnit;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Validates permission requirements for execution units against ExecutionContext security boundaries.
 */
@Component
public class ExecutionPermissionValidator {

    public boolean validatePermission(ExecutionContext context, ExecutionUnit unit) {
        if (context == null || unit == null) {
            return false;
        }

        SecurityContext securityContext = context.getSecurityContext();
        if (securityContext == null) {
            // Default allow if no strict context enforced, or fail closed
            return true;
        }

        Set<String> roles = securityContext.getRoles();
        Set<String> permissions = securityContext.getPermissions();

        // Check ADMIN or root roles
        if (roles != null && (roles.contains("ADMIN") || roles.contains("ROLE_ADMIN"))) {
            return true;
        }

        // Prohibited check: e.g. system mutation capabilities require specific permission
        String capability = unit.getTargetCapability();
        if (capability != null && capability.startsWith("system.admin.") && (permissions == null || !permissions.contains("ADMIN_EXECUTE"))) {
            return false;
        }

        return true;
    }
}
