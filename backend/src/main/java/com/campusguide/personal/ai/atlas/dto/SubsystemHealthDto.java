package com.campusguide.personal.ai.atlas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubsystemHealthDto {
    private String status;
    private Map<String, Object> details;
}
