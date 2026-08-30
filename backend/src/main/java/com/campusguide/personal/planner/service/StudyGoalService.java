package com.campusguide.personal.planner.service;

import com.campusguide.personal.planner.dto.CreateStudyGoalRequest;
import com.campusguide.personal.planner.dto.StudyGoalResponse;
import com.campusguide.personal.planner.dto.UpdateStudyGoalRequest;
import com.campusguide.personal.planner.entity.StudyGoal;
import com.campusguide.personal.planner.exception.PlannerTaskNotFoundException;
import com.campusguide.personal.planner.mapper.StudyGoalMapper;
import com.campusguide.personal.planner.repository.StudyGoalRepository;
import com.campusguide.platform.user.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudyGoalService {

    private final StudyGoalRepository studyGoalRepository;
    private final StudyGoalMapper studyGoalMapper;
    private final CurrentUserService currentUserService;

    public List<StudyGoalResponse> getGoals(UserDetails userDetails) {
        String userId = resolveUserId(userDetails);
        List<StudyGoal> goals = studyGoalRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return goals.stream()
                .map(studyGoalMapper::toResponse)
                .collect(Collectors.toList());
    }

    public StudyGoalResponse getGoalById(UserDetails userDetails, UUID id) {
        String userId = resolveUserId(userDetails);
        StudyGoal goal = findAndVerifyOwnership(id, userId);
        return studyGoalMapper.toResponse(goal);
    }

    public StudyGoalResponse createGoal(UserDetails userDetails, CreateStudyGoalRequest request) {
        String userId = resolveUserId(userDetails);
        StudyGoal goal = studyGoalMapper.toEntity(request, userId);
        StudyGoal saved = studyGoalRepository.save(goal);
        return studyGoalMapper.toResponse(saved);
    }

    public StudyGoalResponse updateGoal(UserDetails userDetails, UUID id, UpdateStudyGoalRequest request) {
        String userId = resolveUserId(userDetails);
        StudyGoal goal = findAndVerifyOwnership(id, userId);

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            goal.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            goal.setDescription(request.getDescription().trim());
        }
        if (request.getTargetHours() != null) {
            goal.setTargetHours(request.getTargetHours());
        }
        if (request.getCompletedHours() != null) {
            goal.setCompletedHours(request.getCompletedHours());
        }
        if (request.getCategory() != null) {
            goal.setCategory(request.getCategory());
        }
        if (request.getDeadline() != null) {
            goal.setDeadline(request.getDeadline());
        }

        if (request.getIsCompleted() != null) {
            goal.setIsCompleted(request.getIsCompleted());
        } else if (goal.getTargetHours() != null && goal.getCompletedHours() != null) {
            if (goal.getCompletedHours() >= goal.getTargetHours()) {
                goal.setIsCompleted(true);
            }
        }

        goal.setUpdatedAt(Instant.now());
        StudyGoal saved = studyGoalRepository.save(goal);
        return studyGoalMapper.toResponse(saved);
    }

    public void deleteGoal(UserDetails userDetails, UUID id) {
        String userId = resolveUserId(userDetails);
        StudyGoal goal = findAndVerifyOwnership(id, userId);
        studyGoalRepository.delete(goal);
    }

    public StudyGoal findAndVerifyOwnership(UUID id, String userId) {
        return studyGoalRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new PlannerTaskNotFoundException("Study goal not found"));
    }

    public String resolveUserId(UserDetails userDetails) {
        return currentUserService.getCurrentUserId(userDetails);
    }
}
