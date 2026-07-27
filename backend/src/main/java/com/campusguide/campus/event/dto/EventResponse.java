package com.campusguide.campus.event.dto;

import com.campusguide.campus.event.entity.EventStatus;
import com.campusguide.campus.event.entity.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventResponse {

    private UUID id;
    private String title;
    private String slug;
    private String description;
    private String summary;
    private UUID councilId;
    private String venue;
    private EventType eventType;
    private EventStatus status;
    private Boolean registrationRequired;
    private LocalDateTime registrationStart;
    private LocalDateTime registrationEnd;
    private Integer capacity;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String bannerUrl;
    private String contactEmail;
    private String contactNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
