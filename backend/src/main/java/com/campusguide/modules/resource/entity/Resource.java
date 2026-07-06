package com.campusguide.modules.resource.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "resources")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resource {

    @Id
    private String id;

    private String title;

    private String description;

    @Indexed
    private String uploaderId;

    @Indexed
    private String councilId;

    @Indexed
    private String communityId;

    private List<String> tags;

    private String fileName;

    private String originalFileName;

    private String fileType;

    private Long fileSize;

    private String downloadUrl;

    @Builder.Default
    private Boolean isDeleted = false;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
