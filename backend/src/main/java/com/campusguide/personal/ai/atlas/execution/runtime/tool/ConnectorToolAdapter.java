package com.campusguide.personal.ai.atlas.execution.runtime.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * ToolAdapter for executing future external connector tools.
 */
@Slf4j
@Component
public class ConnectorToolAdapter implements ToolAdapter {

    @Override
    public boolean supports(String capability) {
        if (capability == null) return false;
        String cap = capability.toLowerCase();
        return cap.startsWith("connector.") || cap.startsWith("external.");
    }

    @Override
    public ToolResult execute(ToolInvocation invocation) {
        long start = System.currentTimeMillis();
        log.info("Executing Connector tool: {} for unit {}", invocation.getCapability(), invocation.getUnitId());

        Map<String, Object> output = new HashMap<>();
        output.put("capability", invocation.getCapability());
        output.put("executedBy", "ConnectorToolAdapter");
        output.put("connectorStatus", "CONNECTED");

        long duration = System.currentTimeMillis() - start;
        return ToolResult.success(invocation.getInvocationId(), invocation.getUnitId(), output, duration);
    }
}
