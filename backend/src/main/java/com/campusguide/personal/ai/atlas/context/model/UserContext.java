package com.campusguide.personal.ai.atlas.context.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Map;

/**
 * Strongly-typed domain context model for User Profile.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserContext {

    private String userId;
    private String name;
    private String email;
    private String role;
    private String status;
    private String summary;

    @Builder.Default
    private Map<String, String> preferences = Collections.emptyMap();
}
