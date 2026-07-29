package com.campusguide.personal.ai.atlas.context.service.knowledge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NavigationMetadataInfo {
    private String routeId;
    private String origin;
    private String destination;
    private double distanceMeters;
    private int estimatedWalkMinutes;
    private boolean accessible;
}
