package com.campusguide.campus.event.dto;

import com.campusguide.campus.event.entity.EventStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateEventStatusRequest {

    @NotNull(message = "Status is required")
    private EventStatus status;
}
