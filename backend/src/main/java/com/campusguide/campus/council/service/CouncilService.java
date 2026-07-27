package com.campusguide.campus.council.service;

import com.campusguide.campus.community.repository.CommunityRepository;
import com.campusguide.campus.council.dto.CouncilResponse;
import com.campusguide.campus.council.dto.CreateCouncilRequest;
import com.campusguide.campus.council.dto.UpdateCouncilRequest;
import com.campusguide.campus.council.dto.UpdateCouncilStatusRequest;
import com.campusguide.campus.council.entity.Council;
import com.campusguide.campus.council.exception.CouncilHasDependenciesException;
import com.campusguide.campus.council.exception.CouncilNotFoundException;
import com.campusguide.campus.council.exception.DuplicateCouncilException;
import com.campusguide.campus.council.mapper.CouncilMapper;
import com.campusguide.campus.council.repository.CouncilRepository;
import com.campusguide.campus.event.repository.EventRepository;
import com.campusguide.campus.resource.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CouncilService {

    private final CouncilRepository councilRepository;
    private final CouncilMapper councilMapper;
    private final CommunityRepository communityRepository;
    private final EventRepository eventRepository;
    private final ResourceRepository resourceRepository;

    /**
     * Creates a new council.
     *
     * @param request creation request containing details
     * @return created CouncilResponse
     * @throws DuplicateCouncilException if name or slug already exists
     */
    public CouncilResponse createCouncil(CreateCouncilRequest request) {
        if (councilRepository.existsByName(request.getName())) {
            throw new DuplicateCouncilException("Council with name '" + request.getName() + "' already exists");
        }
        if (councilRepository.existsBySlug(request.getSlug())) {
            throw new DuplicateCouncilException("Council with slug '" + request.getSlug() + "' already exists");
        }

        Council council = councilMapper.toEntity(request);
        Council saved = councilRepository.save(council);
        return councilMapper.toResponse(saved);
    }

    /**
     * Retrieves all councils.
     *
     * @return list of CouncilResponse
     */
    public List<CouncilResponse> getAllCouncils() {
        List<Council> councils = councilRepository.findAll();
        return councilMapper.toResponseList(councils);
    }

    /**
     * Retrieves a council by its UUID id.
     *
     * @param id council UUID
     * @return CouncilResponse
     * @throws CouncilNotFoundException if no council exists with given id
     */
    public CouncilResponse getCouncilById(UUID id) {
        Council council = councilRepository.findById(id)
                .orElseThrow(() -> new CouncilNotFoundException("Council not found with ID: " + id));
        return councilMapper.toResponse(council);
    }

    /**
     * Retrieves a council by its unique slug.
     *
     * @param slug council slug
     * @return CouncilResponse
     * @throws CouncilNotFoundException if no council exists with given slug
     */
    public CouncilResponse getCouncilBySlug(String slug) {
        Council council = councilRepository.findBySlug(slug)
                .orElseThrow(() -> new CouncilNotFoundException("Council not found with slug: " + slug));
        return councilMapper.toResponse(council);
    }

    /**
     * Updates an existing council's properties.
     *
     * @param id council UUID
     * @param request update request containing updated details
     * @return updated CouncilResponse
     * @throws CouncilNotFoundException if no council exists with given id
     * @throws DuplicateCouncilException if updated name or slug collides with another council
     */
    public CouncilResponse updateCouncil(UUID id, UpdateCouncilRequest request) {
        Council council = councilRepository.findById(id)
                .orElseThrow(() -> new CouncilNotFoundException("Council not found with ID: " + id));

        if (councilRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new DuplicateCouncilException("Council with name '" + request.getName() + "' already exists");
        }
        if (councilRepository.existsBySlugAndIdNot(request.getSlug(), id)) {
            throw new DuplicateCouncilException("Council with slug '" + request.getSlug() + "' already exists");
        }

        councilMapper.updateEntityFromRequest(council, request);
        Council updated = councilRepository.save(council);
        return councilMapper.toResponse(updated);
    }

    /**
     * Soft activates/deactivates a council.
     *
     * @param id council UUID
     * @param request request containing new active status
     * @return updated CouncilResponse
     * @throws CouncilNotFoundException if no council exists with given id
     */
    public CouncilResponse updateCouncilStatus(UUID id, UpdateCouncilStatusRequest request) {
        Council council = councilRepository.findById(id)
                .orElseThrow(() -> new CouncilNotFoundException("Council not found with ID: " + id));

        council.setIsActive(request.getIsActive());
        council.setUpdatedAt(LocalDateTime.now());

        Council updated = councilRepository.save(council);
        return councilMapper.toResponse(updated);
    }

    /**
     * Deletes a council by UUID id.
     * Rejects deletion if dependent entities (communities, events, resources) exist.
     *
     * @param id council UUID
     * @throws CouncilNotFoundException if no council exists with given id
     * @throws CouncilHasDependenciesException if dependent entities exist
     */
    public void deleteCouncil(UUID id) {
        Council council = councilRepository.findById(id)
                .orElseThrow(() -> new CouncilNotFoundException("Council not found with ID: " + id));

        if (hasDependentEntities(id)) {
            throw new CouncilHasDependenciesException("Cannot delete council with ID '" + id + "' because dependent entities exist");
        }

        councilRepository.delete(council);
    }

    /**
     * Checks if dependent entities exist for a council.
     *
     * @param councilId council UUID
     * @return true if dependent entities exist, false otherwise
     */
    public boolean hasDependentEntities(UUID councilId) {
        String idStr = councilId.toString();

        boolean hasCommunities = communityRepository != null && !communityRepository.findByCouncilId(idStr).isEmpty();
        boolean hasEvents = eventRepository != null && !eventRepository.findByCouncilIdAndIsDeletedFalse(idStr).isEmpty();
        boolean hasResources = resourceRepository != null && !resourceRepository.findByCouncilIdAndIsDeletedFalseOrderByCreatedAtDesc(idStr).isEmpty();

        return hasCommunities || hasEvents || hasResources;
    }
}
