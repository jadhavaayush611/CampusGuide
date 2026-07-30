package com.campusguide.personal.ai.atlas.execution.runtime.human;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Command submitted for human control of workflow execution.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum CommandType {
        PAUSE,
        RESUME,
        CANCEL,
        RETRY,
        APPROVE,
        REJECT,
        OVERRIDE
    }

    @Builder.Default
    private String commandId = "cmd_" + UUID.randomUUID().toString().substring(0, 8);

    private String workflowId;
    private String instanceId;
    private String unitId;
    private CommandType commandType;
    private String reason;
    private String operatorId;

    @Builder.Default
    private Map<String, Object> overrideParameters = new HashMap<>();
}
