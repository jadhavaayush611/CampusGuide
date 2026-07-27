package com.campusguide.personal.achievement.service;

import com.campusguide.common.exception.UnauthorisedException;
import com.campusguide.personal.achievement.dto.AchievementProgressResponse;
import com.campusguide.personal.achievement.dto.CreateAchievementRequest;
import com.campusguide.personal.achievement.dto.UpdateAchievementProgressRequest;
import com.campusguide.personal.achievement.dto.UpdateAchievementRequest;
import com.campusguide.personal.achievement.entity.AchievementCategory;
import com.campusguide.personal.achievement.entity.AchievementProgress;
import com.campusguide.personal.achievement.entity.AchievementStatus;
import com.campusguide.personal.achievement.exception.AchievementAccessDeniedException;
import com.campusguide.personal.achievement.exception.AchievementAlreadyExistsException;
import com.campusguide.personal.achievement.exception.AchievementNotFoundException;
import com.campusguide.personal.achievement.mapper.AchievementProgressMapper;
import com.campusguide.personal.achievement.repository.AchievementProgressRepository;
import com.campusguide.personal.achievement.validation.AchievementValidator;
import com.campusguide.platform.user.entity.User;
import com.campusguide.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AchievementProgressService {

    private final AchievementProgressRepository achievementProgressRepository;
    private final AchievementProgressMapper achievementProgressMapper;
    private final AchievementValidator achievementValidator;
    private final UserRepository userRepository;

    public AchievementProgressResponse createAchievement(UserDetails userDetails, CreateAchievementRequest request) {
        UUID userId = resolveUserId(userDetails);
        achievementValidator.validateCreate(request);

        if (achievementProgressRepository.existsByUserIdAndAchievementCode(userId, request.getAchievementCode())) {
            throw new AchievementAlreadyExistsException("Achievement code already exists for user: " + request.getAchievementCode());
        }

        LocalDateTime now = LocalDateTime.now();
        int progress = request.getProgress() != null ? request.getProgress() : 0;
        AchievementStatus status = computeStatus(progress, null);
        LocalDateTime earnedAt = (status == AchievementStatus.EARNED) ? now : null;

        AchievementProgress achievement = AchievementProgress.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .achievementCode(request.getAchievementCode())
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .status(status)
                .progress(progress)
                .earnedAt(earnedAt)
                .evidenceUrl(request.getEvidenceUrl())
                .metadata(request.getMetadata())
                .createdAt(now)
                .updatedAt(now)
                .build();

        AchievementProgress saved = achievementProgressRepository.save(achievement);
        return achievementProgressMapper.toResponse(saved);
    }

    public List<AchievementProgressResponse> getAchievements(UserDetails userDetails, AchievementCategory category, AchievementStatus status) {
        UUID userId = resolveUserId(userDetails);
        List<AchievementProgress> achievements;

        if (category != null && status != null) {
            achievements = achievementProgressRepository.findByUserIdAndCategoryAndStatus(userId, category, status);
        } else if (category != null) {
            achievements = achievementProgressRepository.findByUserIdAndCategory(userId, category);
        } else if (status != null) {
            achievements = achievementProgressRepository.findByUserIdAndStatus(userId, status);
        } else {
            achievements = achievementProgressRepository.findByUserId(userId);
        }

        return achievements.stream()
                .map(achievementProgressMapper::toResponse)
                .collect(Collectors.toList());
    }

    public AchievementProgressResponse getAchievementById(UserDetails userDetails, UUID id) {
        UUID userId = resolveUserId(userDetails);
        AchievementProgress achievement = findAndVerifyOwnership(id, userId);
        return achievementProgressMapper.toResponse(achievement);
    }

    public AchievementProgressResponse updateAchievement(UserDetails userDetails, UUID id, UpdateAchievementRequest request) {
        UUID userId = resolveUserId(userDetails);
        AchievementProgress achievement = findAndVerifyOwnership(id, userId);

        achievementValidator.validateUpdate(achievement, request);

        LocalDateTime now = LocalDateTime.now();
        achievement.setTitle(request.getTitle());
        achievement.setDescription(request.getDescription());
        achievement.setCategory(request.getCategory());
        achievement.setEvidenceUrl(request.getEvidenceUrl());
        achievement.setMetadata(request.getMetadata());

        if (request.getProgress() != null) {
            int newProgress = request.getProgress();
            achievement.setProgress(newProgress);
            AchievementStatus newStatus = computeStatus(newProgress, achievement.getStatus());
            achievement.setStatus(newStatus);
            if (newStatus == AchievementStatus.EARNED && achievement.getEarnedAt() == null) {
                achievement.setEarnedAt(now);
            }
        }

        achievement.setUpdatedAt(now);
        AchievementProgress saved = achievementProgressRepository.save(achievement);
        return achievementProgressMapper.toResponse(saved);
    }

    public AchievementProgressResponse updateProgress(UserDetails userDetails, UUID id, UpdateAchievementProgressRequest request) {
        UUID userId = resolveUserId(userDetails);
        AchievementProgress achievement = findAndVerifyOwnership(id, userId);

        achievementValidator.validateProgressUpdate(achievement, request);

        LocalDateTime now = LocalDateTime.now();
        int newProgress = request.getProgress();
        achievement.setProgress(newProgress);

        AchievementStatus newStatus = computeStatus(newProgress, achievement.getStatus());
        achievement.setStatus(newStatus);
        if (newStatus == AchievementStatus.EARNED && achievement.getEarnedAt() == null) {
            achievement.setEarnedAt(now);
        }

        achievement.setUpdatedAt(now);
        AchievementProgress saved = achievementProgressRepository.save(achievement);
        return achievementProgressMapper.toResponse(saved);
    }

    public void deleteAchievement(UserDetails userDetails, UUID id) {
        UUID userId = resolveUserId(userDetails);
        AchievementProgress achievement = findAndVerifyOwnership(id, userId);
        achievementProgressRepository.delete(achievement);
    }

    public AchievementProgress findAndVerifyOwnership(UUID id, UUID userId) {
        AchievementProgress achievement = achievementProgressRepository.findById(id)
                .orElseThrow(() -> new AchievementNotFoundException("Achievement progress not found with id: " + id));

        if (!achievement.getUserId().equals(userId)) {
            throw new AchievementAccessDeniedException("User is not authorized to access this achievement");
        }

        return achievement;
    }

    private AchievementStatus computeStatus(int progress, AchievementStatus currentStatus) {
        if (progress == 100) {
            return AchievementStatus.EARNED;
        }
        if (currentStatus == AchievementStatus.EARNED) {
            return AchievementStatus.EARNED;
        }
        if (progress > 0) {
            return AchievementStatus.IN_PROGRESS;
        }
        return AchievementStatus.LOCKED;
    }

    public UUID resolveUserId(UserDetails userDetails) {
        if (userDetails == null) {
            throw new UnauthorisedException("User is not authenticated");
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseGet(() -> userRepository.findByUsername(userDetails.getUsername())
                        .orElseThrow(() -> new UnauthorisedException("User not found: " + userDetails.getUsername())));

        return parseUserId(user.getId());
    }

    private UUID parseUserId(String idStr) {
        if (idStr == null) {
            throw new UnauthorisedException("User ID is missing");
        }
        try {
            return UUID.fromString(idStr);
        } catch (IllegalArgumentException e) {
            return UUID.nameUUIDFromBytes(idStr.getBytes(StandardCharsets.UTF_8));
        }
    }
}
