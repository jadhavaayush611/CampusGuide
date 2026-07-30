package com.campusguide.personal.ai.atlas.decision.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

/**
 * Boundaries and allowed domains/action types for decision generation and selection.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DecisionScope implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String domainFilter = "*";

    @Builder.Default
    private int maximumDepth = 3;

    @Builder.Default
    private Set<String> allowedActionTypes = Collections.singleton("*");

    @Builder.Default
    private Set<String> restrictedActionTypes = Collections.emptySet();

    @Builder.Default
    private Set<String> systemBoundaries = Collections.emptySet();

    public static DecisionScope defaultScope() {
        return DecisionScope.builder()
                .domainFilter("*")
                .maximumDepth(3)
                .allowedActionTypes(Collections.singleton("*"))
                .restrictedActionTypes(Collections.emptySet())
                .systemBoundaries(Collections.emptySet())
                .build();
    }
}
