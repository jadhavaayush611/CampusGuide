package com.campusguide.academic.roadmap.service;

import com.campusguide.common.exception.BadRequestException;
import com.campusguide.common.exception.ResourceNotFoundException;
import com.campusguide.common.exception.UnauthorisedException;
import com.campusguide.academic.roadmap.dto.CreateRoadmapRequest;
import com.campusguide.academic.roadmap.dto.RoadmapResponse;
import com.campusguide.academic.roadmap.dto.RoadmapSummaryResponse;
import com.campusguide.academic.roadmap.dto.UpdateRoadmapRequest;
import com.campusguide.academic.roadmap.entity.Roadmap;
import com.campusguide.academic.roadmap.repository.RoadmapRepository;
import com.campusguide.platform.user.entity.Role;
import com.campusguide.platform.user.entity.User;
import com.campusguide.platform.user.repository.UserRepository;
import com.campusguide.personal.notification.service.interfaces.NotificationService;
import com.campusguide.personal.notification.enums.NotificationType;
import com.campusguide.personal.notification.enums.NotificationPriority;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoadmapService {

    private final RoadmapRepository roadmapRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * Creates a new academic roadmap.
     *
     * @param userDetails the authenticated user details
     * @param request the create roadmap request
     * @return the created RoadmapResponse
     */
    public RoadmapResponse createRoadmap(UserDetails userDetails, CreateRoadmapRequest request) {
        if (userDetails == null) {
            throw new UnauthorisedException("User is not authenticated");
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userDetails.getUsername()));

        if (request.getTitle() == null || request.getTitle().trim().isBlank()) {
            throw new BadRequestException("Title cannot be blank");
        }

        if (request.getDegreeProgram() == null || request.getDegreeProgram().isBlank()) {
            throw new BadRequestException("Degree program cannot be blank");
        }

        if (request.getDepartment() == null || request.getDepartment().isBlank()) {
            throw new BadRequestException("Department cannot be blank");
        }

        if (request.getTotalCredits() == null || request.getTotalCredits() < 1) {
            throw new BadRequestException("Total credits must be at least 1");
        }

        if (request.getExpectedGraduationYear() == null || request.getExpectedGraduationYear() < 2000) {
            throw new BadRequestException("Expected graduation year must be at least 2000");
        }

        Roadmap roadmap = Roadmap.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .degreeProgram(request.getDegreeProgram().trim())
                .department(request.getDepartment().trim())
                .totalCredits(request.getTotalCredits())
                .expectedGraduationYear(request.getExpectedGraduationYear())
                .createdBy(user.getId())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        roadmap = roadmapRepository.save(roadmap);
        notificationService.createNotification(
                user.getId(),
                "Roadmap Published",
                "Your academic roadmap '" + roadmap.getTitle() + "' has been successfully published.",
                NotificationType.ACADEMIC,
                NotificationPriority.NORMAL,
                null
        );
        return toRoadmapResponse(roadmap);
    }


    /**
     * Updates an existing roadmap.
     *
     * @param userDetails the authenticated user details
     * @param roadmapId the ID of the roadmap to update
     * @param request the update request
     * @return the updated RoadmapResponse
     */
    public RoadmapResponse updateRoadmap(UserDetails userDetails, String roadmapId, UpdateRoadmapRequest request) {
        if (userDetails == null) {
            throw new UnauthorisedException("User is not authenticated");
        }

        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap not found with id: " + roadmapId));

        if (Boolean.TRUE.equals(roadmap.getIsDeleted())) {
            throw new ResourceNotFoundException("Roadmap not found with id: " + roadmapId);
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userDetails.getUsername()));

        boolean isCreator = roadmap.getCreatedBy().equals(user.getId());
        boolean isSuperAdmin = user.getRole() == Role.SUPER_ADMIN;

        if (!isCreator && !isSuperAdmin) {
            throw new AccessDeniedException("You are not authorized to update this roadmap");
        }

        if (request.getTitle() != null) {
            if (request.getTitle().trim().isBlank()) {
                throw new BadRequestException("Title cannot be blank");
            }
            roadmap.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            roadmap.setDescription(request.getDescription());
        }
        if (request.getDegreeProgram() != null) {
            if (request.getDegreeProgram().isBlank()) {
                throw new BadRequestException("Degree program cannot be blank");
            }
            roadmap.setDegreeProgram(request.getDegreeProgram().trim());
        }
        if (request.getDepartment() != null) {
            if (request.getDepartment().isBlank()) {
                throw new BadRequestException("Department cannot be blank");
            }
            roadmap.setDepartment(request.getDepartment().trim());
        }
        if (request.getTotalCredits() != null) {
            if (request.getTotalCredits() < 1) {
                throw new BadRequestException("Total credits must be at least 1");
            }
            roadmap.setTotalCredits(request.getTotalCredits());
        }
        if (request.getExpectedGraduationYear() != null) {
            if (request.getExpectedGraduationYear() < 2000) {
                throw new BadRequestException("Expected graduation year must be at least 2000");
            }
            roadmap.setExpectedGraduationYear(request.getExpectedGraduationYear());
        }

        roadmap.setUpdatedAt(LocalDateTime.now());
        roadmap = roadmapRepository.save(roadmap);
        return toRoadmapResponse(roadmap);
    }

    /**
     * Soft deletes a roadmap.
     *
     * @param userDetails the authenticated user details
     * @param roadmapId the ID of the roadmap to delete
     */
    public void deleteRoadmap(UserDetails userDetails, String roadmapId) {
        if (userDetails == null) {
            throw new UnauthorisedException("User is not authenticated");
        }

        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap not found with id: " + roadmapId));

        if (Boolean.TRUE.equals(roadmap.getIsDeleted())) {
            throw new ResourceNotFoundException("Roadmap not found with id: " + roadmapId);
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userDetails.getUsername()));

        boolean isCreator = roadmap.getCreatedBy().equals(user.getId());
        boolean isSuperAdmin = user.getRole() == Role.SUPER_ADMIN;

        if (!isCreator && !isSuperAdmin) {
            throw new AccessDeniedException("You are not authorized to delete this roadmap");
        }

        roadmap.setIsDeleted(true);
        roadmap.setUpdatedAt(LocalDateTime.now());
        roadmapRepository.save(roadmap);
    }

    /**
     * Retrieves an active roadmap by its ID.
     *
     * @param roadmapId the ID of the roadmap
     * @return the roadmap details
     */
    public RoadmapResponse getRoadmapById(String roadmapId) {
        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap not found with id: " + roadmapId));

        if (Boolean.TRUE.equals(roadmap.getIsDeleted())) {
            throw new ResourceNotFoundException("Roadmap not found with id: " + roadmapId);
        }

        return toRoadmapResponse(roadmap);
    }

    /**
     * Retrieves all active roadmaps, newest first.
     *
     * @return list of roadmap summaries
     */
    public List<RoadmapSummaryResponse> getAllRoadmaps() {
        return roadmapRepository.findByIsDeletedFalseOrderByCreatedAtDesc().stream()
                .map(this::toRoadmapSummaryResponse)
                .toList();
    }

    /**
     * Retrieves active roadmaps created by a specific user.
     *
     * @param creatorId the ID of the creator
     * @return list of roadmap summaries
     */
    public List<RoadmapSummaryResponse> getRoadmapsByCreator(String creatorId) {
        if (!userRepository.existsById(creatorId)) {
            throw new ResourceNotFoundException("User not found with id: " + creatorId);
        }
        return roadmapRepository.findByCreatedByAndIsDeletedFalseOrderByCreatedAtDesc(creatorId).stream()
                .map(this::toRoadmapSummaryResponse)
                .toList();
    }

    /**
     * Retrieves active roadmaps for a specific degree program.
     *
     * @param degreeProgram the degree program
     * @return list of roadmap summaries
     */
    public List<RoadmapSummaryResponse> getRoadmapsByDegree(String degreeProgram) {
        if (degreeProgram == null || degreeProgram.trim().isEmpty()) {
            throw new BadRequestException("Degree program cannot be blank");
        }
        return roadmapRepository.findByDegreeProgramIgnoreCaseAndIsDeletedFalseOrderByCreatedAtDesc(degreeProgram.trim()).stream()
                .map(this::toRoadmapSummaryResponse)
                .toList();
    }

    /**
     * Retrieves active roadmaps for a specific department.
     *
     * @param department the department
     * @return list of roadmap summaries
     */
    public List<RoadmapSummaryResponse> getRoadmapsByDepartment(String department) {
        if (department == null || department.trim().isEmpty()) {
            throw new BadRequestException("Department cannot be blank");
        }
        return roadmapRepository.findByDepartmentIgnoreCaseAndIsDeletedFalseOrderByCreatedAtDesc(department.trim()).stream()
                .map(this::toRoadmapSummaryResponse)
                .toList();
    }

    private RoadmapResponse toRoadmapResponse(Roadmap roadmap) {
        if (roadmap == null) {
            return null;
        }
        return RoadmapResponse.builder()
                .id(roadmap.getId())
                .title(roadmap.getTitle())
                .description(roadmap.getDescription())
                .degreeProgram(roadmap.getDegreeProgram())
                .department(roadmap.getDepartment())
                .totalCredits(roadmap.getTotalCredits())
                .expectedGraduationYear(roadmap.getExpectedGraduationYear())
                .createdBy(roadmap.getCreatedBy())
                .createdAt(roadmap.getCreatedAt())
                .updatedAt(roadmap.getUpdatedAt())
                .build();
    }

    private RoadmapSummaryResponse toRoadmapSummaryResponse(Roadmap roadmap) {
        if (roadmap == null) {
            return null;
        }
        return RoadmapSummaryResponse.builder()
                .id(roadmap.getId())
                .title(roadmap.getTitle())
                .degreeProgram(roadmap.getDegreeProgram())
                .department(roadmap.getDepartment())
                .expectedGraduationYear(roadmap.getExpectedGraduationYear())
                .build();
    }
}
