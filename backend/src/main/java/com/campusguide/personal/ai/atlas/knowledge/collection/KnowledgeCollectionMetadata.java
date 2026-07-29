package com.campusguide.personal.ai.atlas.knowledge.collection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.*;

/**
 * Metadata associated with a KnowledgeCollection defining ownership, permissions, domain categories, and weights.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeCollectionMetadata implements Serializable {

    private static final long serialVersionUID = 1L;

    private String ownerId;
    @Builder.Default
    private String ownerType = "SYSTEM";

    @Builder.Default
    private Set<String> allowedRoles = new HashSet<>();

    @Builder.Default
    private Set<String> allowedUsers = new HashSet<>();

    @Builder.Default
    private boolean isPublic = true;

    @Builder.Default
    private double priority = 1.0;

    private String category;
    private String domain;

    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Builder.Default
    private Map<String, Object> customAttributes = new HashMap<>();

    public boolean isUserAuthorized(String userId, List<String> userRoles) {
        if (isPublic) return true;
        if (userId != null && ownerId != null && userId.equals(ownerId)) return true;
        if (userId != null && allowedUsers != null && allowedUsers.contains(userId)) return true;

        if (userRoles != null && allowedRoles != null && !allowedRoles.isEmpty()) {
            for (String role : userRoles) {
                if (allowedRoles.contains(role) || allowedRoles.contains("ROLE_" + role.toUpperCase())) {
                    return true;
                }
            }
        }
        return false;
    }
}
