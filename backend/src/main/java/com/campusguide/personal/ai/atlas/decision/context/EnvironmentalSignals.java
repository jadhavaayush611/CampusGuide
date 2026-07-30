package com.campusguide.personal.ai.atlas.decision.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

/**
 * Real-time environmental signals (system load, time of day, network status, client capabilities).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnvironmentalSignals implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private double systemLoad = 0.10;

    @Builder.Default
    private String timeOfDay = "DAY";

    @Builder.Default
    private NetworkStatus networkStatus = NetworkStatus.ONLINE;

    @Builder.Default
    private Set<String> clientCapabilities = Collections.singleton("RICH_TEXT");

    @Builder.Default
    private boolean emergencyMode = false;

    public enum NetworkStatus {
        ONLINE,
        DEGRADED,
        OFFLINE
    }

    public static EnvironmentalSignals defaultSignals() {
        return EnvironmentalSignals.builder()
                .systemLoad(0.10)
                .timeOfDay("DAY")
                .networkStatus(NetworkStatus.ONLINE)
                .clientCapabilities(Collections.singleton("RICH_TEXT"))
                .emergencyMode(false)
                .build();
    }
}
