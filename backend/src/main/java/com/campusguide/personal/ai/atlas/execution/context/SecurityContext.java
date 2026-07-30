package com.campusguide.personal.ai.atlas.execution.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Security context specifying roles, permissions, and isolation levels.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecurityContext implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private Set<String> roles = new HashSet<>();

    @Builder.Default
    private Set<String> permissions = new HashSet<>();

    @Builder.Default
    private Set<String> securityTokens = new HashSet<>();

    @Builder.Default
    private String isolationLevel = "STANDARD";

    private String authenticatedUserId;

    public static SecurityContext defaultContext() {
        Set<String> roles = new HashSet<>();
        roles.add("USER");
        Set<String> permissions = new HashSet<>();
        permissions.add("EXECUTE_BASIC_WORKFLOW");

        return SecurityContext.builder()
                .roles(roles)
                .permissions(permissions)
                .isolationLevel("STANDARD")
                .authenticatedUserId("system")
                .build();
    }
}
