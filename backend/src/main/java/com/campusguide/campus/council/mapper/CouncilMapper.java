package com.campusguide.campus.council.mapper;

import com.campusguide.campus.council.dto.CouncilResponse;
import com.campusguide.campus.council.dto.CreateCouncilRequest;
import com.campusguide.campus.council.dto.UpdateCouncilRequest;
import com.campusguide.campus.council.entity.Council;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class CouncilMapper {

    public Council toEntity(CreateCouncilRequest request) {
        if (request == null) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        return Council.builder()
                .id(UUID.randomUUID())
                .name(trim(request.getName()))
                .slug(trim(request.getSlug()))
                .description(trim(request.getDescription()))
                .logoUrl(trim(request.getLogoUrl()))
                .email(trim(request.getEmail()))
                .contactNumber(trim(request.getContactNumber()))
                .facultyAdvisor(trim(request.getFacultyAdvisor()))
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void updateEntityFromRequest(Council council, UpdateCouncilRequest request) {
        if (council == null || request == null) {
            return;
        }

        council.setName(trim(request.getName()));
        council.setSlug(trim(request.getSlug()));
        council.setDescription(trim(request.getDescription()));
        council.setLogoUrl(trim(request.getLogoUrl()));
        council.setEmail(trim(request.getEmail()));
        council.setContactNumber(trim(request.getContactNumber()));
        council.setFacultyAdvisor(trim(request.getFacultyAdvisor()));
        if (request.getIsActive() != null) {
            council.setIsActive(request.getIsActive());
        }
        council.setUpdatedAt(LocalDateTime.now());
    }

    private String trim(String value) {
        return value != null ? value.trim() : null;
    }

    public CouncilResponse toResponse(Council council) {
        if (council == null) {
            return null;
        }

        return CouncilResponse.builder()
                .id(council.getId())
                .name(council.getName())
                .slug(council.getSlug())
                .description(council.getDescription())
                .logoUrl(council.getLogoUrl())
                .email(council.getEmail())
                .contactNumber(council.getContactNumber())
                .facultyAdvisor(council.getFacultyAdvisor())
                .isActive(council.getIsActive())
                .createdAt(council.getCreatedAt())
                .updatedAt(council.getUpdatedAt())
                .build();
    }

    public List<CouncilResponse> toResponseList(List<Council> councils) {
        if (councils == null) {
            return Collections.emptyList();
        }

        return councils.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
