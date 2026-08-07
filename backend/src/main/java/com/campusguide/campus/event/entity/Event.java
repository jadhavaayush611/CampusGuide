package com.campusguide.campus.event.entity;

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

@Document(collection = "events")
@CompoundIndexes({
    @CompoundIndex(name = "status_endtime_starttime_idx", def = "{'status': 1, 'endTime': 1, 'startTime': 1}")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @jakarta.validation.constraints.NotNull(message = "ID must not be null")
    private UUID id;

    @jakarta.validation.constraints.NotBlank(message = "Title must not be blank")
    private String title;

    @jakarta.validation.constraints.NotBlank(message = "Slug must not be blank")
    @Indexed(unique = true)
    private String slug;

    private String description;

    private String summary;

    @jakarta.validation.constraints.NotNull(message = "Council ID must not be null")
    @Indexed
    private UUID councilId;

    private String venue;

    private EventType eventType;

    private EventStatus status;

    private Boolean registrationRequired;

    private LocalDateTime registrationStart;

    private LocalDateTime registrationEnd;

    private Integer capacity;

    @jakarta.validation.constraints.NotNull(message = "Start time must not be null")
    private LocalDateTime startTime;

    @jakarta.validation.constraints.NotNull(message = "End time must not be null")
    private LocalDateTime endTime;

    private String bannerUrl;

    @jakarta.validation.constraints.Email(message = "Contact email must be valid")
    private String contactEmail;

    private String contactNumber;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @org.springframework.data.annotation.Version
    private Long version;

    public static class EventBuilder {
        public EventBuilder createdAt(Instant instant) {
            this.createdAt = instant;
            return this;
        }

        public EventBuilder createdAt(LocalDateTime dateTime) {
            this.createdAt = dateTime != null ? dateTime.atZone(ZoneId.systemDefault()).toInstant() : null;
            return this;
        }

        public EventBuilder updatedAt(Instant instant) {
            this.updatedAt = instant;
            return this;
        }

        public EventBuilder updatedAt(LocalDateTime dateTime) {
            this.updatedAt = dateTime != null ? dateTime.atZone(ZoneId.systemDefault()).toInstant() : null;
            return this;
        }
    }
}
