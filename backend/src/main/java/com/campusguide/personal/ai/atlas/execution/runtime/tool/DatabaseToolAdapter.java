package com.campusguide.personal.ai.atlas.execution.runtime.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * ToolAdapter for database operations.
 */
@Slf4j
@Component
public class DatabaseToolAdapter implements ToolAdapter {

    @Override
    public boolean supports(String capability) {
        if (capability == null) return false;
        String cap = capability.toLowerCase();
        return cap.startsWith("db.") || cap.startsWith("database.") || cap.startsWith("sql.");
    }

    @Override
    public ToolResult execute(ToolInvocation invocation) {
        long start = System.currentTimeMillis();
        log.info("Executing Database tool: {} for unit {}", invocation.getCapability(), invocation.getUnitId());

        Map<String, Object> output = new HashMap<>();
        output.put("capability", invocation.getCapability());
        output.put("executedBy", "DatabaseToolAdapter");
        output.put("rowsAffected", 1);

        long duration = System.currentTimeMillis() - start;
        return ToolResult.success(invocation.getInvocationId(), invocation.getUnitId(), output, duration);
    }
}
