package com.campusguide.modules.council.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "councils")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Council {

    @Id
    private String id;

    @Indexed(unique = true)
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
