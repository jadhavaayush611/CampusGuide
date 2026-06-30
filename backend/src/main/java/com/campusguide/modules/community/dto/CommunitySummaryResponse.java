package com.campusguide.modules.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunitySummaryResponse {

    private String id;

    private String name;

    private String bannerUrl;

    private Integer memberCount;
}
