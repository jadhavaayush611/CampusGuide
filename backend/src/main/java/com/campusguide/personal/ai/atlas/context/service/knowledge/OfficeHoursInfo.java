package com.campusguide.personal.ai.atlas.context.service.knowledge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfficeHoursInfo {
    private String id;
    private String facultyId;
    private String facultyName;
    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private String location;
    private String notes;
}
