package com.campusguide.personal.ai.atlas.execution.runtime.tool;

import com.campusguide.personal.ai.atlas.execution.context.SecurityContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Invocation request payload for ToolAdapters.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolInvocation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String invocationId = "inv_" + UUID.randomUUID().toString().substring(0, 8);

    private String workflowId;
    private String contextId;
    private String unitId;
    private String capability;

    @Builder.Default
    private Map<String, Object> payload = new HashMap<>();

    @Builder.Default
    private long timeoutSeconds = 60L;

    private SecurityContext securityContext;
    private String userId;
}
