package com.campusguide.personal.ai.atlas.context.service.knowledge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentServiceInfo {
    private String serviceId;
    private String name;
    private String category;
    private String location;
    private String contactInfo;
    private String operatingHours;
}
