package com.campusguide.campus.council.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCouncilStatusRequest {

    @NotNull(message = "isActive status must not be null")
    private Boolean isActive;
}
