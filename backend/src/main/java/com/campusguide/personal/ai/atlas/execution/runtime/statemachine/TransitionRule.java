package com.campusguide.personal.ai.atlas.execution.runtime.statemachine;

import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Encapsulates a valid state transition rule in the runtime state machine.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransitionRule implements Serializable {

    private static final long serialVersionUID = 1L;

    private WorkflowState fromState;
    private WorkflowState toState;
    private String description;
}
