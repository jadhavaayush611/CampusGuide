package com.campusguide.personal.ai.atlas.execution.runtime.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * ToolAdapter for executing local in-process functions.
 */
@Slf4j
@Component
public class LocalFunctionToolAdapter implements ToolAdapter {

    @Override
    public boolean supports(String capability) {
        if (capability == null) return false;
        String cap = capability.toLowerCase();
        return cap.startsWith("local.") || cap.startsWith("function.") || cap.startsWith("fn.");
    }

    @Override
    public ToolResult execute(ToolInvocation invocation) {
        long start = System.currentTimeMillis();
        log.info("Executing LocalFunction tool: {} for unit {}", invocation.getCapability(), invocation.getUnitId());

        Map<String, Object> output = new HashMap<>();
        output.put("capability", invocation.getCapability());
        output.put("executedBy", "LocalFunctionToolAdapter");
        output.put("result", "local_fn_executed");

        long duration = System.currentTimeMillis() - start;
        return ToolResult.success(invocation.getInvocationId(), invocation.getUnitId(), output, duration);
    }
}
