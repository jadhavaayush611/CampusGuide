package com.campusguide.personal.achievement.controller;

import com.campusguide.personal.achievement.dto.AchievementProgressResponse;
import com.campusguide.personal.achievement.dto.CreateAchievementRequest;
import com.campusguide.personal.achievement.dto.UpdateAchievementProgressRequest;
import com.campusguide.personal.achievement.dto.UpdateAchievementRequest;
import com.campusguide.personal.achievement.entity.AchievementCategory;
import com.campusguide.personal.achievement.entity.AchievementStatus;
import com.campusguide.personal.achievement.service.AchievementProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/achievements")
@RequiredArgsConstructor
@Validated
public class AchievementProgressController {

    private final AchievementProgressService achievementProgressService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AchievementProgressResponse> createAchievement(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateAchievementRequest request) {
        AchievementProgressResponse response = achievementProgressService.createAchievement(userDetails, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AchievementProgressResponse>> getAchievements(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) AchievementCategory category,
            @RequestParam(required = false) AchievementStatus status) {
        List<AchievementProgressResponse> response = achievementProgressService.getAchievements(userDetails, category, status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AchievementProgressResponse> getAchievementById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        AchievementProgressResponse response = achievementProgressService.getAchievementById(userDetails, id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/progress")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AchievementProgressResponse> updateProgress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAchievementProgressRequest request) {
        AchievementProgressResponse response = achievementProgressService.updateProgress(userDetails, id, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AchievementProgressResponse> updateAchievement(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAchievementRequest request) {
        AchievementProgressResponse response = achievementProgressService.updateAchievement(userDetails, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteAchievement(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        achievementProgressService.deleteAchievement(userDetails, id);
        return ResponseEntity.noContent().build();
    }
}
