package com.campusguide.personal.ai.atlas.planning.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * Time horizon bounds for plan scheduling.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeHorizon implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private Instant startTime = Instant.now();

    private Instant endTime;

    @Builder.Default
    private double maxDurationMinutes = 1440.0; // 24 hours default

    public static TimeHorizon defaultHorizon() {
        return TimeHorizon.builder()
                .startTime(Instant.now())
                .maxDurationMinutes(1440.0)
                .build();
    }
}
