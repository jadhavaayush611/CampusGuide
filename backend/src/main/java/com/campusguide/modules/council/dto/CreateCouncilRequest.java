package com.campusguide.modules.council.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCouncilRequest {

    @NotBlank(message = "Council name is required")
    private String name;

    @NotBlank(message = "Council description is required")
    private String description;

    private String logoUrl;

    @NotBlank(message = "Council category is required")
    private String category;

    private String facultyAdvisorId;
}
