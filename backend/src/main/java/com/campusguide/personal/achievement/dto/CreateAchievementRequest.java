package com.campusguide.personal.achievement.dto;

import com.campusguide.personal.achievement.entity.AchievementCategory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAchievementRequest {

    @NotBlank(message = "Achievement code is required")
    private String achievementCode;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Category is required")
    private AchievementCategory category;

    @Min(value = 0, message = "Progress must be at least 0")
    @Max(value = 100, message = "Progress must be at most 100")
    private Integer progress;

    private String evidenceUrl;

    private Map<String, Object> metadata;
}
