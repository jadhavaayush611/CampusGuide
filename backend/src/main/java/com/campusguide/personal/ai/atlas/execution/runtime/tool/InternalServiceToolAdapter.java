package com.campusguide.personal.ai.atlas.execution.runtime.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * ToolAdapter for executing internal CampusGuide platform and domain service invocations.
 */
@Slf4j
@Component
public class InternalServiceToolAdapter implements ToolAdapter {

    @Override
    public boolean supports(String capability) {
        if (capability == null) return false;
        String cap = capability.toLowerCase();
        return cap.startsWith("internal.") || cap.startsWith("campus.") ||
               cap.startsWith("academic.") || cap.startsWith("calendar.") ||
               cap.startsWith("planner.");
    }

    @Override
    public ToolResult execute(ToolInvocation invocation) {
        long start = System.currentTimeMillis();
        log.info("Executing internal service tool: {} for unit {}", invocation.getCapability(), invocation.getUnitId());

        Map<String, Object> output = new HashMap<>();
        output.put("capability", invocation.getCapability());
        output.put("executedBy", "InternalServiceToolAdapter");
        output.put("status", "COMPLETED");
        if (invocation.getPayload() != null) {
            output.putAll(invocation.getPayload());
        }

        long duration = System.currentTimeMillis() - start;
        return ToolResult.success(invocation.getInvocationId(), invocation.getUnitId(), output, duration);
    }
}
