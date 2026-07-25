package com.campusguide.campus.community.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

@Document(collection = "communities")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Community {

    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    private String description;

    private String bannerUrl;

    @Indexed
    private String councilId;

    private Integer memberCount;

    private Boolean isActive;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
