package com.campusguide.personal.ai.atlas.execution.risk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Individual risk factor evaluated during execution risk assessment.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskFactor implements Serializable {

    private static final long serialVersionUID = 1L;

    private String factorId;
    private RiskFactorCategory category;
    private String name;
    private double score; // 0.0 to 1.0
    private double weight; // 0.0 to 1.0
    private String description;
}
