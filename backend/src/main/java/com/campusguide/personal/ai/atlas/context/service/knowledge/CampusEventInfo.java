package com.campusguide.personal.ai.atlas.context.service.knowledge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampusEventInfo {
    private String eventId;
    private String title;
    private String description;
    private String location;
    private String startTime;
    private String endTime;
    private String organizer;
}
