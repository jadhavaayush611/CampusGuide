package com.campusguide.modules.council.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouncilResponse {

    private String id;

    private String name;

    private String description;

    private String logoUrl;

    private String category;

    private String facultyAdvisorId;

    private List<String> councilAdminIds;

    private Integer memberCount;

    private Boolean isActive;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
