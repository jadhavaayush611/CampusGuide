package com.campusguide.personal.ai.atlas.execution.runtime.security;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.context.SecurityContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ExecutionSecurityManagerTest {

    private ExecutionSecurityManager securityManager;

    @BeforeEach
    void setUp() {
        ExecutionPermissionValidator validator = new ExecutionPermissionValidator();
        securityManager = new ExecutionSecurityManager(validator);
    }

    @Test
    void testAuthorizedStandardExecution() {
        ExecutionContext context = ExecutionContext.builder()
                .userId("user_123")
                .securityContext(SecurityContext.defaultContext())
                .build();

        ExecutionUnit unit = ExecutionUnit.builder()
                .unitId("unit_search")
                .targetCapability("campus.navigation.search")
                .build();

        boolean authorized = securityManager.authorizeExecution(context, unit, "wf_1");
        assertTrue(authorized);

        List<SecurityAudit> auditTrail = securityManager.getAuditTrail();
        assertFalse(auditTrail.isEmpty());
        assertEquals("ALLOWED", auditTrail.get(0).getAction());
    }

    @Test
    void testDeniedAdminActionWithoutPermissions() {
        Set<String> roles = new HashSet<>();
        roles.add("STUDENT");

        SecurityContext restricted = SecurityContext.builder()
                .roles(roles)
                .permissions(Collections.emptySet())
                .build();

        ExecutionContext context = ExecutionContext.builder()
                .userId("student_1")
                .securityContext(restricted)
                .build();

        ExecutionUnit unit = ExecutionUnit.builder()
                .unitId("unit_admin_action")
                .targetCapability("system.admin.reboot")
                .build();

        boolean authorized = securityManager.authorizeExecution(context, unit, "wf_restricted");
        assertFalse(authorized);

        List<SecurityAudit> auditTrail = securityManager.getAuditTrail();
        assertEquals("DENIED", auditTrail.get(auditTrail.size() - 1).getAction());
    }
}
