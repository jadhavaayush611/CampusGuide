package com.campusguide.campus.resource.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceSummaryResponse {

    private String id;

    private String title;

    private String fileType;

    private Long fileSize;

    private String uploaderId;

    private LocalDateTime createdAt;
}
