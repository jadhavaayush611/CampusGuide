package com.campusguide.personal.ai.atlas.planning.scheduling;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Evaluated candidate schedule generated during scheduling strategy execution.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleCandidate implements Serializable {

    private static final long serialVersionUID = 1L;

    private String candidateId;
    private Schedule schedule;
    private double score;
    private String strategyName;

    @Builder.Default
    private List<String> violations = new ArrayList<>();
}
