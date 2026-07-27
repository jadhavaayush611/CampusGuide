package com.campusguide.personal.calendar.dto;

import com.campusguide.personal.calendar.entity.CalendarEntryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateCalendarEntryRequest {

    @NotBlank(message = "Title is mandatory")
    private String title;

    private String description;

    @NotNull(message = "Calendar entry type is mandatory")
    private CalendarEntryType type;

    private UUID linkedPlannerTaskId;

    private UUID linkedEventId;

    private String location;

    @NotNull(message = "startTime is mandatory")
    private LocalDateTime startTime;

    @NotNull(message = "endTime is mandatory")
    private LocalDateTime endTime;

    private Boolean isAllDay;

    private String color;

    private String notes;
}
