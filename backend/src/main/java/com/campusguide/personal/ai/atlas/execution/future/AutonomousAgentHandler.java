package com.campusguide.personal.ai.atlas.execution.future;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;

/**
 * SPI interface supporting autonomous agent delegation.
 */
public interface AutonomousAgentHandler {

    boolean canDelegateToAgent(ExecutionContext context, ExecutableWorkflow workflow);

    void prepareAgentDelegation(ExecutionContext context, ExecutableWorkflow workflow);
}
