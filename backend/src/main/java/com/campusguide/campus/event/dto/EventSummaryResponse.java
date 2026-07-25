package com.campusguide.campus.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventSummaryResponse {

    private String id;

    private String title;

    private String councilId;

    private String location;

    private LocalDateTime startTime;

    private Integer attendeeCount;

    private Integer maxParticipants;

    private String imageUrl;
}
