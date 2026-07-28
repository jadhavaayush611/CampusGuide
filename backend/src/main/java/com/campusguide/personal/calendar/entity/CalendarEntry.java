package com.campusguide.personal.calendar.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Document(collection = "calendar_entries")
@CompoundIndexes({
    @CompoundIndex(name = "user_start_end_idx", def = "{'userId': 1, 'startTime': 1, 'endTime': 1}")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarEntry {

    @Id
    private UUID id;

    @Indexed
    private String userId;

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
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public static class CalendarEntryBuilder {
        public CalendarEntryBuilder createdAt(Instant instant) {
            this.createdAt = instant;
            return this;
        }

        public CalendarEntryBuilder createdAt(LocalDateTime dateTime) {
            this.createdAt = dateTime != null ? dateTime.atZone(ZoneId.systemDefault()).toInstant() : null;
            return this;
        }

        public CalendarEntryBuilder updatedAt(Instant instant) {
            this.updatedAt = instant;
            return this;
        }

        public CalendarEntryBuilder updatedAt(LocalDateTime dateTime) {
            this.updatedAt = dateTime != null ? dateTime.atZone(ZoneId.systemDefault()).toInstant() : null;
            return this;
        }
    }
}
