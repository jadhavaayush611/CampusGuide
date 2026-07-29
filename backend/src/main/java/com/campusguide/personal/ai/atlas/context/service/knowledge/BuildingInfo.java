package com.campusguide.personal.ai.atlas.context.service.knowledge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuildingInfo {
    private String buildingId;
    private String name;
    private String code;
    private double latitude;
    private double longitude;
    private String address;
    private String operatingHours;
    private List<String> departments;
}
