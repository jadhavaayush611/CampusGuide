package com.campusguide.personal.planner.mapper;

import com.campusguide.personal.planner.dto.CreateStudyGoalRequest;
import com.campusguide.personal.planner.dto.StudyGoalResponse;
import com.campusguide.personal.planner.entity.StudyGoal;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class StudyGoalMapper {

    public StudyGoal toEntity(CreateStudyGoalRequest request, String userId) {
        if (request == null) {
            return null;
        }
        Instant now = Instant.now();
        return StudyGoal.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .title(request.getTitle())
                .description(request.getDescription())
                .targetHours(request.getTargetHours())
                .completedHours(0)
                .deadline(request.getDeadline())
                .isCompleted(false)
                .category(request.getCategory() != null && !request.getCategory().isBlank() ? request.getCategory() : "General")
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public StudyGoalResponse toResponse(StudyGoal goal) {
        if (goal == null) {
            return null;
        }
        return StudyGoalResponse.builder()
                .id(goal.getId())
                .userId(goal.getUserId())
                .title(goal.getTitle())
                .description(goal.getDescription())
                .targetHours(goal.getTargetHours())
                .completedHours(goal.getCompletedHours())
                .deadline(goal.getDeadline())
                .isCompleted(goal.getIsCompleted())
                .category(goal.getCategory())
                .createdAt(goal.getCreatedAt())
                .updatedAt(goal.getUpdatedAt())
                .build();
    }
}
