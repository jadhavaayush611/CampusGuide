package com.campusguide.personal.ai.atlas.execution.runtime.tool;

/**
 * Provider-independent interface for tool adapters.
 */
public interface ToolAdapter {

    boolean supports(String capability);

    ToolResult execute(ToolInvocation invocation);
}
