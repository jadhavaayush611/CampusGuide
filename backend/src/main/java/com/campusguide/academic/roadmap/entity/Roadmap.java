package com.campusguide.academic.roadmap.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

@Document(collection = "roadmaps")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Roadmap {

    @Id
    private String id;

    private String title;

    private String description;

    @Indexed
    private String degreeProgram;

    @Indexed
    private String department;

    private Integer totalCredits;

    private Integer expectedGraduationYear;

    @Indexed
    private String createdBy;

    @Builder.Default
    private Boolean isDeleted = false;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
