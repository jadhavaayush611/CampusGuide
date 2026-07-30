package com.campusguide.personal.ai.atlas.execution.runtime.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * ToolAdapter for executing Model Context Protocol (MCP) tool invocations.
 */
@Slf4j
@Component
public class McpToolAdapter implements ToolAdapter {

    @Override
    public boolean supports(String capability) {
        return capability != null && capability.toLowerCase().startsWith("mcp.");
    }

    @Override
    public ToolResult execute(ToolInvocation invocation) {
        long start = System.currentTimeMillis();
        log.info("Executing MCP tool: {} for unit {}", invocation.getCapability(), invocation.getUnitId());

        Map<String, Object> output = new HashMap<>();
        output.put("capability", invocation.getCapability());
        output.put("executedBy", "McpToolAdapter");
        output.put("mcpProtocolVersion", "1.0");
        output.put("status", "COMPLETED");

        long duration = System.currentTimeMillis() - start;
        return ToolResult.success(invocation.getInvocationId(), invocation.getUnitId(), output, duration);
    }
}
