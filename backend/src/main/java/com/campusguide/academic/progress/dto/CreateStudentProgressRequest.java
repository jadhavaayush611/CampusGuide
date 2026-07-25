package com.campusguide.academic.progress.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateStudentProgressRequest {

    @NotBlank(message = "Roadmap ID is required")
    private String roadmapId;
}
