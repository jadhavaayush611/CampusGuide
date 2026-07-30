package com.campusguide.personal.ai.atlas.execution.runtime.rollback;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionUnit;
import com.campusguide.personal.ai.atlas.execution.runtime.tool.ToolExecutor;
import com.campusguide.personal.ai.atlas.execution.runtime.tool.ToolResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Executes individual compensating units (reverse actions) via ToolExecutor.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CompensationExecutor {

    private final ToolExecutor toolExecutor;

    public ToolResult executeCompensatingUnit(ExecutionContext context, ExecutionUnit compensatingUnit, String workflowId) {
        if (compensatingUnit == null) {
            return ToolResult.failure("inv_compensate_null", "unit_null", "Null compensating unit provided", 0L);
        }

        log.info("Executing compensating unit {} (capability: {}) for workflow {}",
                compensatingUnit.getUnitId(), compensatingUnit.getTargetCapability(), workflowId);

        return toolExecutor.executeUnit(context, compensatingUnit, workflowId);
    }
}
