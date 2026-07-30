package com.campusguide.personal.ai.atlas.execution.runtime.tool;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.context.SecurityContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionUnit;
import com.campusguide.personal.ai.atlas.execution.runtime.retry.RetryEngine;
import com.campusguide.personal.ai.atlas.execution.runtime.security.ExecutionPermissionValidator;
import com.campusguide.personal.ai.atlas.execution.runtime.security.ExecutionSecurityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ToolExecutorTest {

    private ToolExecutor toolExecutor;
    private ExecutionSecurityManager securityManager;
    private ToolRegistry toolRegistry;

    @BeforeEach
    void setUp() {
        InternalServiceToolAdapter internalAdapter = new InternalServiceToolAdapter();
        McpToolAdapter mcpAdapter = new McpToolAdapter();
        RestApiToolAdapter restAdapter = new RestApiToolAdapter();

        toolRegistry = new ToolRegistry(List.of(internalAdapter, mcpAdapter, restAdapter));
        ExecutionPermissionValidator permissionValidator = new ExecutionPermissionValidator();
        securityManager = new ExecutionSecurityManager(permissionValidator);
        RetryEngine retryEngine = new RetryEngine();

        toolExecutor = new ToolExecutor(toolRegistry, securityManager, retryEngine);
    }

    @Test
    void testSuccessfulToolExecution() {
        ExecutionContext context = ExecutionContext.builder()
                .contextId("ctx_test")
                .userId("user_test")
                .securityContext(SecurityContext.defaultContext())
                .build();

        ExecutionUnit unit = ExecutionUnit.builder()
                .unitId("unit_tool_1")
                .targetCapability("internal.campus.search")
                .payload(new HashMap<>())
                .approvalRequired(false)
                .build();

        ToolResult result = toolExecutor.executeUnit(context, unit, "wf_test");

        assertNotNull(result);
        assertEquals(ToolExecutionStatus.SUCCESS, result.getStatus());
        assertEquals("InternalServiceToolAdapter", result.getOutputData().get("executedBy"));
    }

    @Test
    void testApprovalRequiredToolReturnsWaitingState() {
        ExecutionContext context = ExecutionContext.builder()
                .contextId("ctx_test")
                .securityContext(SecurityContext.defaultContext())
                .build();

        ExecutionUnit unit = ExecutionUnit.builder()
                .unitId("unit_approval")
                .targetCapability("internal.mutation")
                .approvalRequired(true)
                .build();

        ToolResult result = toolExecutor.executeUnit(context, unit, "wf_test");

        assertNotNull(result);
        assertEquals(ToolExecutionStatus.WAITING_FOR_APPROVAL, result.getStatus());
    }

    @Test
    void testDeniedSecurityCheckReturnsDenied() {
        SecurityContext restrictedContext = SecurityContext.builder()
                .roles(Collections.singleton("USER"))
                .permissions(Collections.emptySet())
                .build();

        ExecutionContext context = ExecutionContext.builder()
                .contextId("ctx_restricted")
                .securityContext(restrictedContext)
                .build();

        ExecutionUnit unit = ExecutionUnit.builder()
                .unitId("unit_admin")
                .targetCapability("system.admin.shutdown")
                .build();

        ToolResult result = toolExecutor.executeUnit(context, unit, "wf_test");

        assertNotNull(result);
        assertEquals(ToolExecutionStatus.DENIED, result.getStatus());
    }
}
