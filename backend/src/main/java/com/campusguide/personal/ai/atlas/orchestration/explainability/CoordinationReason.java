package com.campusguide.personal.ai.atlas.orchestration.explainability;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Explanation model capturing the rationale behind multi-agent synchronization and coordination.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoordinationReason {

    private String reasonId;
    private String coordinationType;
    private String description;
    @Builder.Default
    private List<String> participatingAgents = new ArrayList<>();
}
