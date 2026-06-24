package com.campusguide.modules.council.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouncilSummaryResponse {

    private String id;

    private String name;

    private String logoUrl;

    private String category;

    private Integer memberCount;
}
