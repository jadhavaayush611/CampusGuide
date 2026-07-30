package com.campusguide.personal.ai.atlas.execution.runtime.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Security audit entry capturing runtime tool authorization decisions.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecurityAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String auditId = "sec_audit_" + UUID.randomUUID().toString().substring(0, 8);

    private String workflowId;
    private String unitId;
    private String capability;
    private String userId;
    private String action; // ALLOWED, DENIED
    private String reason;

    @Builder.Default
    private Instant timestamp = Instant.now();
}
