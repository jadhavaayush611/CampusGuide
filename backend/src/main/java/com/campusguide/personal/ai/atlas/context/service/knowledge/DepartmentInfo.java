package com.campusguide.personal.ai.atlas.context.service.knowledge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentInfo {
    private String deptId;
    private String name;
    private String code;
    private String buildingId;
    private String headOfDepartment;
    private String contactEmail;
    private String phone;
}
