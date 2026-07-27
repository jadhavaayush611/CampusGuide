package com.campusguide.campus.event.dto;

import com.campusguide.campus.event.entity.EventType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateEventRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Slug is required")
    private String slug;

    @NotBlank(message = "Description is required")
    private String description;

    private String summary;

    @NotBlank(message = "Venue is required")
    private String venue;

    @NotNull(message = "Event type is required")
    private EventType eventType;

    private Boolean registrationRequired;

    private LocalDateTime registrationStart;

    private LocalDateTime registrationEnd;

    @Positive(message = "Capacity must be positive")
    private Integer capacity;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    private LocalDateTime endTime;

    private String bannerUrl;

    @Email(message = "Contact email must be valid")
    private String contactEmail;

    private String contactNumber;
}
