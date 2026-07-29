package com.campusguide.personal.ai.atlas.context.service.knowledge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacultyInfo {
    private String facultyId;
    private String name;
    private String title;
    private String department;
    private String email;
    private String officeRoom;
    private List<String> researchAreas;
}
