package com.campusguide.personal.calendar.dto;

import com.campusguide.personal.calendar.entity.CalendarEntryType;
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
public class CalendarEntryResponse {

    private UUID id;
    private UUID userId;
    private String title;
    private String description;
    private CalendarEntryType type;
    private UUID linkedPlannerTaskId;
    private UUID linkedEventId;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean isAllDay;
    private String color;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
