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
public class ClassroomInfo {
    private String classroomId;
    private String roomNumber;
    private String buildingId;
    private int capacity;
    private List<String> features;
}
