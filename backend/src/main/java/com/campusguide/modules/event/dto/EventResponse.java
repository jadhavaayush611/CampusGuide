package com.campusguide.modules.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {

    private String id;

    private String title;

    private String description;

    private String councilId;

    private String organizerId;

    private String location;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime registrationDeadline;

    private Integer maxParticipants;

    private Integer attendeeCount;

    private String imageUrl;

    private Boolean isCancelled;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
