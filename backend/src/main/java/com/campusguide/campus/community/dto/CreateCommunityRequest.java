package com.campusguide.campus.community.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommunityRequest {

    @NotBlank(message = "Community name is required")
    private String name;

    @NotBlank(message = "Community description is required")
    private String description;

    private String bannerUrl;

    @NotBlank(message = "Council ID is required")
    private String councilId;
}
