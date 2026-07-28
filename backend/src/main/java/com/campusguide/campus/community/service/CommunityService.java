package com.campusguide.campus.community.service;

import com.campusguide.common.exception.ConflictException;
import com.campusguide.common.exception.ResourceNotFoundException;
import com.campusguide.campus.community.dto.CommunityResponse;
import com.campusguide.campus.community.dto.CommunitySummaryResponse;
import com.campusguide.campus.community.dto.CreateCommunityRequest;
import com.campusguide.campus.community.dto.UpdateCommunityRequest;
import com.campusguide.campus.community.entity.Community;
import com.campusguide.campus.community.repository.CommunityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityRepository communityRepository;

    /**
     * Creates a new community.
     *
     * @param request the request containing details of the community to create
     * @return the created community details
     * @throws ConflictException if a community with the same name already exists
     */
    public CommunityResponse createCommunity(CreateCommunityRequest request) {
        if (communityRepository.existsByName(request.getName())) {
            throw new ConflictException("Community with name '" + request.getName() + "' already exists");
        }

        Community community = Community.builder()
                .name(request.getName())
                .description(request.getDescription())
                .bannerUrl(request.getBannerUrl())
                .councilId(request.getCouncilId())
                .memberCount(0)
                .isActive(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        community = communityRepository.save(community);
        return toCommunityResponse(community);
    }

    /**
     * Updates an existing community's fields.
     *
     * @param communityId the ID of the community to update
     * @param request the request containing updated fields
     * @return the updated community details
     * @throws ResourceNotFoundException if the community with the specified ID does not exist
     */
    public CommunityResponse updateCommunity(String communityId, UpdateCommunityRequest request) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found with id: " + communityId));

        if (request.getDescription() != null) {
            community.setDescription(request.getDescription());
        }
        if (request.getBannerUrl() != null) {
            community.setBannerUrl(request.getBannerUrl());
        }
        if (request.getIsActive() != null) {
            community.setIsActive(request.getIsActive());
        }

        community.setUpdatedAt(Instant.now());
        community = communityRepository.save(community);
        return toCommunityResponse(community);
    }

    /**
     * Retrieves a community by its ID.
     *
     * @param communityId the ID of the community to retrieve
     * @return the community details
     * @throws ResourceNotFoundException if the community does not exist
     */
    public CommunityResponse getCommunityById(String communityId) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found with id: " + communityId));
        return toCommunityResponse(community);
    }

    /**
     * Retrieves all active communities.
     *
     * @return a list of summaries of active communities
     */
    public List<CommunitySummaryResponse> getAllCommunities() {
        return communityRepository.findByIsActiveTrue().stream()
                .map(this::toCommunitySummaryResponse)
                .toList();
    }

    /**
     * Retrieves all communities belonging to a specific council.
     *
     * @param councilId the ID of the council
     * @return a list of summaries of communities in the specified council
     * @throws ResourceNotFoundException if the council does not exist
     */
    public List<CommunitySummaryResponse> getCommunitiesByCouncil(String councilId) {
        return communityRepository.findByCouncilId(councilId).stream()
                .map(this::toCommunitySummaryResponse)
                .toList();
    }

    private CommunityResponse toCommunityResponse(Community community) {
        if (community == null) {
            return null;
        }
        return CommunityResponse.builder()
                .id(community.getId())
                .name(community.getName())
                .description(community.getDescription())
                .bannerUrl(community.getBannerUrl())
                .councilId(community.getCouncilId())
                .memberCount(community.getMemberCount())
                .isActive(community.getIsActive())
                .createdAt(community.getCreatedAt() != null ? java.time.LocalDateTime.ofInstant(community.getCreatedAt(), java.time.ZoneId.systemDefault()) : null)
                .updatedAt(community.getUpdatedAt() != null ? java.time.LocalDateTime.ofInstant(community.getUpdatedAt(), java.time.ZoneId.systemDefault()) : null)
                .build();
    }

    private CommunitySummaryResponse toCommunitySummaryResponse(Community community) {
        if (community == null) {
            return null;
        }
        return CommunitySummaryResponse.builder()
                .id(community.getId())
                .name(community.getName())
                .bannerUrl(community.getBannerUrl())
                .memberCount(community.getMemberCount())
                .build();
    }
}
