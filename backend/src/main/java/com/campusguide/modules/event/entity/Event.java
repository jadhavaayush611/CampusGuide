package com.campusguide.modules.event.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    private String id;

    private String title;

    private String description;

    @Indexed
    private String councilId;

    private String organizerId;

    private String location;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime registrationDeadline;

    private Integer maxParticipants;

    private Integer attendeeCount;

    private List<String> registeredUserIds;

    private String imageUrl;

    private Boolean isCancelled;

    private Boolean isDeleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
