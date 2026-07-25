package com.campusguide.campus.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCommunityRequest {

    private String description;

    private String bannerUrl;

    private Boolean isActive;
}
