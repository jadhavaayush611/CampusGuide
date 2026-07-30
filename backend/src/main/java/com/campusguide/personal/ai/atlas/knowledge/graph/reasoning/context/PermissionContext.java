package com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.context;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

/**
 * Encapsulates security roles and explicit permissions for graph projection and reasoning evaluation.
 */
@Data
@Builder
public class PermissionContext implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;
    private String userRole;

    @Builder.Default
    private Set<String> permissions = Collections.emptySet();

    public static PermissionContext admin() {
        return PermissionContext.builder()
                .userId("admin")
                .userRole("ADMIN")
                .permissions(Set.of("READ_ALL", "WRITE_ALL", "GRAPH_REASON"))
                .build();
    }

    public static PermissionContext anonymous() {
        return PermissionContext.builder()
                .userId("guest")
                .userRole("GUEST")
                .permissions(Set.of("READ_PUBLIC"))
                .build();
    }

    public boolean hasPermission(String permission) {
        if (permissions == null || permissions.isEmpty()) return false;
        return permissions.contains("READ_ALL") || permissions.contains(permission);
    }
}
