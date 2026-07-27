package com.campusguide.campus.council.dto;

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
public class CouncilResponse {

    private UUID id;
    private String name;
    private String slug;
    private String description;
    private String logoUrl;
    private String email;
    private String contactNumber;
    private String facultyAdvisor;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
