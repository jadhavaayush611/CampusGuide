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
public class LaboratoryInfo {
    private String labId;
    private String name;
    private String buildingId;
    private String roomNumber;
    private int capacity;
    private List<String> equipmentList;
}
