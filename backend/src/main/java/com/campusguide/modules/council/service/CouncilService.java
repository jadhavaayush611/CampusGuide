package com.campusguide.modules.council.service;

import com.campusguide.modules.council.entity.Council;
import com.campusguide.modules.council.repository.CouncilRepository;
import com.campusguide.modules.council.dto.CreateCouncilRequest;
import com.campusguide.modules.council.dto.UpdateCouncilRequest;
import com.campusguide.modules.council.dto.CouncilResponse;
import com.campusguide.modules.council.dto.CouncilSummaryResponse;
import com.campusguide.exception.BadRequestException;
import com.campusguide.exception.ConflictException;
import com.campusguide.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouncilService {

    private final CouncilRepository councilRepository;

    /**
     * Creates a new council.
     *
     * @param request the request containing details of the council to create
     * @return the created council details
     * @throws BadRequestException if a council with the same name already exists
     */
    public CouncilResponse createCouncil(CreateCouncilRequest request) {
        if (councilRepository.existsByName(request.getName())) {
            throw new ConflictException("Council with name '" + request.getName() + "' already exists");
        }

        Council council = Council.builder()
                .name(request.getName())
                .description(request.getDescription())
                .logoUrl(request.getLogoUrl())
                .category(request.getCategory())
                .facultyAdvisorId(request.getFacultyAdvisorId())
                .memberCount(0)
                .isActive(true)
                .councilAdminIds(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        council = councilRepository.save(council);
        return toCouncilResponse(council);
    }

    /**
     * Updates an existing council's fields.
     *
     * @param councilId the ID of the council to update
     * @param request the request containing updated fields
     * @return the updated council details
     * @throws ResourceNotFoundException if the council with the specified ID does not exist
     */
    public CouncilResponse updateCouncil(String councilId, UpdateCouncilRequest request) {
        Council council = councilRepository.findById(councilId)
                .orElseThrow(() -> new ResourceNotFoundException("Council not found with id: " + councilId));

        if (request.getDescription() != null) {
            council.setDescription(request.getDescription());
        }
        if (request.getLogoUrl() != null) {
            council.setLogoUrl(request.getLogoUrl());
        }
        if (request.getCategory() != null) {
            council.setCategory(request.getCategory());
        }
        if (request.getFacultyAdvisorId() != null) {
            council.setFacultyAdvisorId(request.getFacultyAdvisorId());
        }
        if (request.getIsActive() != null) {
            council.setIsActive(request.getIsActive());
        }

        council.setUpdatedAt(LocalDateTime.now());
        council = councilRepository.save(council);
        return toCouncilResponse(council);
    }

    /**
     * Retrieves a council by its ID.
     *
     * @param councilId the ID of the council to retrieve
     * @return the council details
     * @throws ResourceNotFoundException if the council does not exist
     */
    public CouncilResponse getCouncilById(String councilId) {
        Council council = councilRepository.findById(councilId)
                .orElseThrow(() -> new ResourceNotFoundException("Council not found with id: " + councilId));
        return toCouncilResponse(council);
    }

    /**
     * Retrieves all active councils.
     *
     * @return a list of summaries of active councils
     */
    public List<CouncilSummaryResponse> getAllCouncils() {
        return councilRepository.findByIsActiveTrue().stream()
                .map(this::toCouncilSummaryResponse)
                .toList();
    }

    /**
     * Retrieves councils matching a specific category.
     *
     * @param category the category to filter by
     * @return a list of summaries of councils in the specified category
     */
    public List<CouncilSummaryResponse> getCouncilsByCategory(String category) {
        return councilRepository.findByCategory(category).stream()
                .map(this::toCouncilSummaryResponse)
                .toList();
    }

    private CouncilResponse toCouncilResponse(Council council) {
        if (council == null) {
            return null;
        }
        return CouncilResponse.builder()
                .id(council.getId())
                .name(council.getName())
                .description(council.getDescription())
                .logoUrl(council.getLogoUrl())
                .category(council.getCategory())
                .facultyAdvisorId(council.getFacultyAdvisorId())
                .councilAdminIds(council.getCouncilAdminIds())
                .memberCount(council.getMemberCount())
                .isActive(council.getIsActive())
                .createdAt(council.getCreatedAt())
                .updatedAt(council.getUpdatedAt())
                .build();
    }

    private CouncilSummaryResponse toCouncilSummaryResponse(Council council) {
        if (council == null) {
            return null;
        }
        return CouncilSummaryResponse.builder()
                .id(council.getId())
                .name(council.getName())
                .logoUrl(council.getLogoUrl())
                .category(council.getCategory())
                .memberCount(council.getMemberCount())
                .build();
    }
}
