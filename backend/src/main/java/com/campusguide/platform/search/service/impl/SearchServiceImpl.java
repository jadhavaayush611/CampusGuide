package com.campusguide.platform.search.service.impl;

import com.campusguide.common.exception.BadRequestException;
import com.campusguide.common.exception.ResourceNotFoundException;
import com.campusguide.campus.academic.course.entity.Course;
import com.campusguide.campus.academic.course.repository.CourseRepository;
import com.campusguide.campus.academic.roadmap.entity.Roadmap;
import com.campusguide.campus.academic.roadmap.repository.RoadmapRepository;
import com.campusguide.campus.community.entity.Community;
import com.campusguide.campus.community.repository.CommunityRepository;
import com.campusguide.campus.event.entity.Event;
import com.campusguide.campus.event.repository.EventRepository;
import com.campusguide.campus.resource.entity.Resource;
import com.campusguide.campus.resource.repository.ResourceRepository;
import com.campusguide.platform.search.dto.request.GlobalSearchRequest;
import com.campusguide.platform.search.dto.response.GlobalSearchResponse;
import com.campusguide.platform.search.dto.response.SearchResultResponse;
import com.campusguide.platform.search.enums.SearchType;
import com.campusguide.platform.search.mapper.SearchMapper;
import com.campusguide.platform.search.service.interfaces.SearchService;
import com.campusguide.platform.search.util.SearchUtil;
import com.campusguide.platform.user.entity.User;
import com.campusguide.platform.user.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchServiceImpl implements SearchService {

    private final CourseRepository courseRepository;
    private final RoadmapRepository roadmapRepository;
    private final CommunityRepository communityRepository;
    private final EventRepository eventRepository;
    private final ResourceRepository resourceRepository;
    private final CurrentUserService currentUserService;
    private final SearchMapper searchMapper;

    @Override
    public GlobalSearchResponse search(UserDetails userDetails, GlobalSearchRequest request, Pageable pageable) {
        if (request == null || request.getQuery() == null || request.getQuery().trim().isEmpty()) {
            throw new BadRequestException("Search query cannot be blank");
        }
        if (request.getQuery().length() > 255) {
            throw new BadRequestException("Search query too long (max 255 characters)");
        }

        // Verify user exists to respect "authenticated access"
        User user = currentUserService.getCurrentUser(userDetails);

        String query = request.getQuery().trim();

        List<SearchType> typesToSearch = request.getTypes();
        if (typesToSearch == null || typesToSearch.isEmpty()) {
            typesToSearch = Arrays.asList(SearchType.values());
        }

        long startTime = System.currentTimeMillis();
        log.info("Starting global search execution for user ID: {} with modules: {}", user.getId(), typesToSearch);

        List<SearchResultResponse> aggregatedResults = new ArrayList<>();

        // 1. Search Courses
        if (typesToSearch.contains(SearchType.COURSE)) {
            List<Course> courses = courseRepository.findByActiveTrueAndCourseNameContainingIgnoreCaseOrActiveTrueAndCourseCodeContainingIgnoreCaseOrActiveTrueAndDescriptionContainingIgnoreCase(
                    query, query, query
            );
            for (Course course : courses) {
                double nameScore = SearchUtil.calculateRelevanceScore(query, course.getCourseName(), course.getDescription(), null);
                double codeScore = SearchUtil.calculateRelevanceScore(query, course.getCourseCode(), null, null);
                double score = Math.max(nameScore, codeScore);
                if (score > 0.0) {
                    aggregatedResults.add(searchMapper.toResponse(course, score));
                }
            }
        }

        // 2. Search Roadmaps
        if (typesToSearch.contains(SearchType.ROADMAP)) {
            List<Roadmap> roadmaps = roadmapRepository.findByIsDeletedFalseAndTitleContainingIgnoreCaseOrIsDeletedFalseAndDescriptionContainingIgnoreCase(
                    query, query
            );
            for (Roadmap roadmap : roadmaps) {
                double score = SearchUtil.calculateRelevanceScore(query, roadmap.getTitle(), roadmap.getDescription(), null);
                if (score > 0.0) {
                    aggregatedResults.add(searchMapper.toResponse(roadmap, score));
                }
            }
        }

        // 3. Search Communities
        if (typesToSearch.contains(SearchType.COMMUNITY)) {
            List<Community> communities = communityRepository.findByIsActiveTrueAndNameContainingIgnoreCaseOrIsActiveTrueAndDescriptionContainingIgnoreCase(
                    query, query
            );
            for (Community community : communities) {
                double score = SearchUtil.calculateRelevanceScore(query, community.getName(), community.getDescription(), null);
                if (score > 0.0) {
                    aggregatedResults.add(searchMapper.toResponse(community, score));
                }
            }
        }

        // 4. Search Events
        if (typesToSearch.contains(SearchType.EVENT)) {
            List<Event> events = eventRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrVenueContainingIgnoreCaseOrderByStartTimeAsc(
                    query, query, query
            );
            for (Event event : events) {
                double score = SearchUtil.calculateRelevanceScore(query, event.getTitle(), event.getDescription(), null);
                if (score > 0.0) {
                    aggregatedResults.add(searchMapper.toResponse(event, score));
                }
            }
        }

        // 5. Search Resources
        if (typesToSearch.contains(SearchType.RESOURCE)) {
            List<Resource> resources = resourceRepository.findByIsDeletedFalseAndTitleContainingIgnoreCaseOrIsDeletedFalseAndDescriptionContainingIgnoreCaseOrIsDeletedFalseAndTagsContainingIgnoreCase(
                    query, query, query
            );
            for (Resource resource : resources) {
                double score = SearchUtil.calculateRelevanceScore(query, resource.getTitle(), resource.getDescription(), resource.getTags());
                if (score > 0.0) {
                    aggregatedResults.add(searchMapper.toResponse(resource, score));
                }
            }
        }

        // Sort by descending relevance score
        aggregatedResults.sort((r1, r2) -> Double.compare(r2.getRelevanceScore(), r1.getRelevanceScore()));

        int totalResults = aggregatedResults.size();
        List<SearchResultResponse> pagedResults;

        if (pageable == null || pageable.isUnpaged()) {
            pagedResults = aggregatedResults;
        } else {
            long offset = pageable.getOffset();
            int pageSize = pageable.getPageSize();
            if (offset >= totalResults) {
                pagedResults = Collections.emptyList();
            } else {
                int start = (int) offset;
                int end = Math.min(start + pageSize, totalResults);
                pagedResults = aggregatedResults.subList(start, end);
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("Global search completed. Found {} results in {} ms", totalResults, duration);

        return GlobalSearchResponse.builder()
                .query(query)
                .totalResults(totalResults)
                .results(pagedResults)
                .build();
    }
}
