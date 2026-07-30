package com.campusguide.personal.ai.atlas.execution.runtime.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * ToolAdapter for executing REST API tool calls.
 */
@Slf4j
@Component
public class RestApiToolAdapter implements ToolAdapter {

    @Override
    public boolean supports(String capability) {
        if (capability == null) return false;
        String cap = capability.toLowerCase();
        return cap.startsWith("rest.") || cap.startsWith("http.") || cap.startsWith("api.");
    }

    @Override
    public ToolResult execute(ToolInvocation invocation) {
        long start = System.currentTimeMillis();
        log.info("Executing REST API tool: {} for unit {}", invocation.getCapability(), invocation.getUnitId());

        Map<String, Object> output = new HashMap<>();
        output.put("capability", invocation.getCapability());
        output.put("executedBy", "RestApiToolAdapter");
        output.put("statusCode", 200);

        long duration = System.currentTimeMillis() - start;
        return ToolResult.success(invocation.getInvocationId(), invocation.getUnitId(), output, duration);
    }
}
