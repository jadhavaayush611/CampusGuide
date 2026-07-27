package com.campusguide.personal.calendar.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "calendar_entries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarEntry {

    @Id
    private UUID id;

    @Indexed
    private UUID userId;

    private String title;

    private String description;

    private CalendarEntryType type;

    @Indexed
    private UUID linkedPlannerTaskId;

    @Indexed
    private UUID linkedEventId;

    private String location;

    @Indexed
    private LocalDateTime startTime;

    @Indexed
    private LocalDateTime endTime;

    @Builder.Default
    private boolean isAllDay = false;

    private String color;

    private String notes;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
