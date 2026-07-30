package com.campusguide.personal.ai.atlas.execution.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates validation output for workflow execution readiness.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private boolean valid = true;

    @Builder.Default
    private List<String> violations = new ArrayList<>();

    @Builder.Default
    private List<String> warnings = new ArrayList<>();

    @Builder.Default
    private int checkedRulesCount = 0;

    @Builder.Default
    private Instant evaluatedAt = Instant.now();

    public static ValidationResult valid() {
        return ValidationResult.builder()
                .valid(true)
                .violations(new ArrayList<>())
                .warnings(new ArrayList<>())
                .checkedRulesCount(1)
                .evaluatedAt(Instant.now())
                .build();
    }

    public static ValidationResult invalid(List<String> violations) {
        return ValidationResult.builder()
                .valid(false)
                .violations(violations != null ? violations : new ArrayList<>())
                .warnings(new ArrayList<>())
                .checkedRulesCount(1)
                .evaluatedAt(Instant.now())
                .build();
    }
}
