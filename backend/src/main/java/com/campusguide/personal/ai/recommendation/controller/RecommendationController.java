package com.campusguide.personal.ai.recommendation.controller;

import com.campusguide.common.exception.BadRequestException;
import com.campusguide.personal.ai.recommendation.dto.RecommendationResponse;
import com.campusguide.personal.ai.recommendation.dto.RecommendationType;
import com.campusguide.personal.ai.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai/recommendations")
@RequiredArgsConstructor
@Validated
public class RecommendationController {

    private final RecommendationService recommendationService;

    /**
     * GET /api/ai/recommendations
     * Returns all recommendation categories for the authenticated user.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RecommendationResponse>> getAllRecommendations(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        List<RecommendationResponse> response = recommendationService.getRecommendations(userDetails, null, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/ai/recommendations/{type}
     * Returns recommendations of the specified category for the authenticated user.
     * Supported types: academic, events, communities, resources
     */
    @GetMapping("/{type}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RecommendationResponse>> getRecommendationsByType(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String type,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        RecommendationType recommendationType = mapType(type);
        List<RecommendationResponse> response = recommendationService.getRecommendations(userDetails, recommendationType, page, size);
        return ResponseEntity.ok(response);
    }

    private RecommendationType mapType(String type) {
        if (type == null) {
            return null;
        }
        return switch (type.toLowerCase().trim()) {
            case "academic" -> RecommendationType.ACADEMIC;
            case "events" -> RecommendationType.EVENT;
            case "communities" -> RecommendationType.COMMUNITY;
            case "resources" -> RecommendationType.RESOURCE;
            default -> throw new BadRequestException("Unsupported recommendation type: " + type);
        };
    }
}
