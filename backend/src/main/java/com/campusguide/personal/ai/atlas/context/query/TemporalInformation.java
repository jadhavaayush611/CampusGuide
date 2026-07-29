package com.campusguide.personal.ai.atlas.context.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Structured temporal expression range resulting from query temporal resolution.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemporalInformation {
    private String rawExpression;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String resolutionType;
    private boolean resolved;
}
