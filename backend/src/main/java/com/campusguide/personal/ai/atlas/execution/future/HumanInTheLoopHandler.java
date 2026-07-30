package com.campusguide.personal.ai.atlas.execution.future;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;

/**
 * SPI interface supporting human-in-the-loop approval and interaction.
 */
public interface HumanInTheLoopHandler {

    boolean requiresHumanIntervention(ExecutionContext context, ExecutableWorkflow workflow);

    void prepareHumanPrompt(ExecutionContext context, ExecutableWorkflow workflow);
}
