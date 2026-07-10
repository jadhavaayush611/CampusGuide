package com.campusguide.modules.resource.service;

import com.campusguide.exception.BadRequestException;
import com.campusguide.exception.ResourceNotFoundException;
import com.campusguide.exception.UnauthorisedException;
import com.campusguide.modules.community.repository.CommunityRepository;
import com.campusguide.modules.council.repository.CouncilRepository;
import com.campusguide.modules.resource.dto.CreateResourceRequest;
import com.campusguide.modules.resource.dto.ResourceResponse;
import com.campusguide.modules.resource.dto.ResourceSummaryResponse;
import com.campusguide.modules.resource.dto.UpdateResourceRequest;
import com.campusguide.modules.resource.entity.Resource;
import com.campusguide.modules.resource.repository.ResourceRepository;
import com.campusguide.modules.user.entity.Role;
import com.campusguide.modules.user.entity.User;
import com.campusguide.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final CouncilRepository councilRepository;
    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;

    /**
     * Creates a new resource metadata record.
     *
     * @param userDetails the authenticated user details
     * @param request the create resource request
     * @return the created ResourceResponse
     */
    public ResourceResponse createResource(UserDetails userDetails, CreateResourceRequest request) {
        if (userDetails == null) {
            throw new UnauthorisedException("User is not authenticated");
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userDetails.getUsername()));

        String councilId = null;
        if (request.getCouncilId() != null && !request.getCouncilId().isBlank()) {
            if (!councilRepository.existsById(request.getCouncilId())) {
                throw new ResourceNotFoundException("Council not found with id: " + request.getCouncilId());
            }
            councilId = request.getCouncilId();
        }

        String communityId = null;
        if (request.getCommunityId() != null && !request.getCommunityId().isBlank()) {
            if (!communityRepository.existsById(request.getCommunityId())) {
                throw new ResourceNotFoundException("Community not found with id: " + request.getCommunityId());
            }
            communityId = request.getCommunityId();
        }

        Resource resource = Resource.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .uploaderId(user.getId())
                .councilId(councilId)
                .communityId(communityId)
                .tags(request.getTags())
                .fileName(request.getFileName())
                .originalFileName(request.getOriginalFileName())
                .fileType(request.getFileType())
                .fileSize(request.getFileSize())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        resource = resourceRepository.save(resource);
        resource.setDownloadUrl("/api/resources/download/" + resource.getId());
        resource = resourceRepository.save(resource);
        return toResourceResponse(resource);
    }

    /**
     * Updates an existing resource.
     *
     * @param userDetails the authenticated user details
     * @param resourceId the ID of the resource to update
     * @param request the update request
     * @return the updated ResourceResponse
     */
    public ResourceResponse updateResource(UserDetails userDetails, String resourceId, UpdateResourceRequest request) {
        if (userDetails == null) {
            throw new UnauthorisedException("User is not authenticated");
        }

        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + resourceId));

        if (Boolean.TRUE.equals(resource.getIsDeleted())) {
            throw new ResourceNotFoundException("Resource not found with id: " + resourceId);
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userDetails.getUsername()));

        boolean isUploader = resource.getUploaderId().equals(user.getId());
        boolean isSuperAdmin = user.getRole() == Role.SUPER_ADMIN;

        if (!isUploader && !isSuperAdmin) {
            throw new AccessDeniedException("You are not authorized to update this resource");
        }

        if (request.getTitle() != null) {
            resource.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            resource.setDescription(request.getDescription());
        }
        if (request.getTags() != null) {
            resource.setTags(request.getTags());
        }

        resource.setUpdatedAt(LocalDateTime.now());
        resource = resourceRepository.save(resource);
        return toResourceResponse(resource);
    }

    /**
     * Soft deletes a resource.
     *
     * @param userDetails the authenticated user details
     * @param resourceId the ID of the resource to delete
     */
    public void deleteResource(UserDetails userDetails, String resourceId) {
        if (userDetails == null) {
            throw new UnauthorisedException("User is not authenticated");
        }

        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + resourceId));

        if (Boolean.TRUE.equals(resource.getIsDeleted())) {
            throw new ResourceNotFoundException("Resource not found with id: " + resourceId);
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userDetails.getUsername()));

        boolean isUploader = resource.getUploaderId().equals(user.getId());
        boolean isSuperAdmin = user.getRole() == Role.SUPER_ADMIN;

        if (!isUploader && !isSuperAdmin) {
            throw new AccessDeniedException("You are not authorized to delete this resource");
        }

        resource.setIsDeleted(true);
        resource.setUpdatedAt(LocalDateTime.now());
        resourceRepository.save(resource);
    }

    /**
     * Retrieves an active resource by its ID.
     *
     * @param resourceId the ID of the resource
     * @return the resource details
     */
    public ResourceResponse getResourceById(String resourceId) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + resourceId));

        if (Boolean.TRUE.equals(resource.getIsDeleted())) {
            throw new ResourceNotFoundException("Resource not found with id: " + resourceId);
        }

        return toResourceResponse(resource);
    }

    /**
     * Retrieves all active resources, newest first.
     *
     * @return list of resource summaries
     */
    public List<ResourceSummaryResponse> getAllResources() {
        return resourceRepository.findByIsDeletedFalseOrderByCreatedAtDesc().stream()
                .map(this::toResourceSummaryResponse)
                .toList();
    }

    /**
     * Retrieves active resources uploaded by a specific user.
     *
     * @param uploaderId the ID of the uploader
     * @return list of resource summaries
     */
    public List<ResourceSummaryResponse> getResourcesByUploader(String uploaderId) {
        if (!userRepository.existsById(uploaderId)) {
            throw new ResourceNotFoundException("User not found with id: " + uploaderId);
        }
        return resourceRepository.findByUploaderIdAndIsDeletedFalseOrderByCreatedAtDesc(uploaderId).stream()
                .map(this::toResourceSummaryResponse)
                .toList();
    }

    /**
     * Retrieves active resources associated with a specific council.
     *
     * @param councilId the ID of the council
     * @return list of resource summaries
     */
    public List<ResourceSummaryResponse> getResourcesByCouncil(String councilId) {
        if (!councilRepository.existsById(councilId)) {
            throw new ResourceNotFoundException("Council not found with id: " + councilId);
        }
        return resourceRepository.findByCouncilIdAndIsDeletedFalseOrderByCreatedAtDesc(councilId).stream()
                .map(this::toResourceSummaryResponse)
                .toList();
    }

    /**
     * Retrieves active resources associated with a specific community.
     *
     * @param communityId the ID of the community
     * @return list of resource summaries
     */
    public List<ResourceSummaryResponse> getResourcesByCommunity(String communityId) {
        if (!communityRepository.existsById(communityId)) {
            throw new ResourceNotFoundException("Community not found with id: " + communityId);
        }
        return resourceRepository.findByCommunityIdAndIsDeletedFalseOrderByCreatedAtDesc(communityId).stream()
                .map(this::toResourceSummaryResponse)
                .toList();
    }

    /**
     * Searches for active resources by title and description (case-insensitive, partial match).
     *
     * @param query the keyword to search
     * @return list of matching resource summaries
     */
    public List<ResourceSummaryResponse> searchResources(String query) {
        if (query == null || query.isBlank()) {
            return getAllResources();
        }
        String trimmedQuery = query.trim();
        return resourceRepository.findByTitleContainingIgnoreCaseAndIsDeletedFalseOrDescriptionContainingIgnoreCaseAndIsDeletedFalseOrderByCreatedAtDesc(trimmedQuery, trimmedQuery).stream()
                .map(this::toResourceSummaryResponse)
                .toList();
    }

    /**
     * Retrieves active resources filtered by a specific tag.
     *
     * @param tag the tag to filter by
     * @return list of resource summaries
     */
    public List<ResourceSummaryResponse> getResourcesByTag(String tag) {
        if (tag == null || tag.trim().isEmpty()) {
            throw new BadRequestException("Tag cannot be blank");
        }
        return resourceRepository.findByTagsIgnoreCaseAndIsDeletedFalseOrderByCreatedAtDesc(tag.trim()).stream()
                .map(this::toResourceSummaryResponse)
                .toList();
    }

    /**
     * Retrieves the latest active resources, newest first.
     *
     * @return list of resource summaries
     */
    public List<ResourceSummaryResponse> getRecentResources() {
        return resourceRepository.findByIsDeletedFalseOrderByCreatedAtDesc().stream()
                .map(this::toResourceSummaryResponse)
                .toList();
    }

    private ResourceResponse toResourceResponse(Resource resource) {
        if (resource == null) {
            return null;
        }
        return ResourceResponse.builder()
                .id(resource.getId())
                .title(resource.getTitle())
                .description(resource.getDescription())
                .uploaderId(resource.getUploaderId())
                .councilId(resource.getCouncilId())
                .communityId(resource.getCommunityId())
                .tags(resource.getTags())
                .fileName(resource.getFileName())
                .originalFileName(resource.getOriginalFileName())
                .fileType(resource.getFileType())
                .fileSize(resource.getFileSize())
                .downloadUrl(resource.getDownloadUrl())
                .createdAt(resource.getCreatedAt())
                .updatedAt(resource.getUpdatedAt())
                .build();
    }

    private ResourceSummaryResponse toResourceSummaryResponse(Resource resource) {
        if (resource == null) {
            return null;
        }
        return ResourceSummaryResponse.builder()
                .id(resource.getId())
                .title(resource.getTitle())
                .fileType(resource.getFileType())
                .fileSize(resource.getFileSize())
                .uploaderId(resource.getUploaderId())
                .createdAt(resource.getCreatedAt())
                .build();
    }
}
