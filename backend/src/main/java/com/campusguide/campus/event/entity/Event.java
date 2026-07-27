package com.campusguide.campus.event.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    private UUID id;

    private String title;

    @Indexed(unique = true)
    private String slug;

    private String description;

    private String summary;

    @Indexed
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
