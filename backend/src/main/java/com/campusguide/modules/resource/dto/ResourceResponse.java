package com.campusguide.modules.resource.dto;

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
public class ResourceResponse {

    private String id;

    private String title;

    private String description;

    private String uploaderId;

    private String councilId;

    private String communityId;

    private List<String> tags;

    private String fileName;

    private String originalFileName;

    private String fileType;

    private Long fileSize;

    private String downloadUrl;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
