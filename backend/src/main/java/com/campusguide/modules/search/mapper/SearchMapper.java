package com.campusguide.modules.search.mapper;

import com.campusguide.modules.course.entity.Course;
import com.campusguide.modules.roadmap.entity.Roadmap;
import com.campusguide.modules.community.entity.Community;
import com.campusguide.modules.event.entity.Event;
import com.campusguide.modules.resource.entity.Resource;
import com.campusguide.modules.search.dto.response.SearchResultResponse;
import com.campusguide.modules.search.enums.SearchType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SearchMapper {

    public SearchResultResponse toResponse(Course course, Double relevanceScore) {
        if (course == null) {
            return null;
        }
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("courseCode", course.getCourseCode());
        metadata.put("department", course.getDepartment());
        metadata.put("credits", course.getCredits());
        metadata.put("semester", course.getSemester());
        metadata.put("elective", course.getElective());

        return SearchResultResponse.builder()
                .id(course.getId())
                .title(course.getCourseName())
                .description(course.getDescription())
                .searchType(SearchType.COURSE)
                .relevanceScore(relevanceScore)
                .metadata(metadata)
                .build();
    }

    public SearchResultResponse toResponse(Roadmap roadmap, Double relevanceScore) {
        if (roadmap == null) {
            return null;
        }
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("degreeProgram", roadmap.getDegreeProgram());
        metadata.put("department", roadmap.getDepartment());
        metadata.put("totalCredits", roadmap.getTotalCredits());
        metadata.put("expectedGraduationYear", roadmap.getExpectedGraduationYear());
        metadata.put("createdBy", roadmap.getCreatedBy());

        return SearchResultResponse.builder()
                .id(roadmap.getId())
                .title(roadmap.getTitle())
                .description(roadmap.getDescription())
                .searchType(SearchType.ROADMAP)
                .relevanceScore(relevanceScore)
                .metadata(metadata)
                .build();
    }

    public SearchResultResponse toResponse(Community community, Double relevanceScore) {
        if (community == null) {
            return null;
        }
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("councilId", community.getCouncilId());
        metadata.put("memberCount", community.getMemberCount());

        return SearchResultResponse.builder()
                .id(community.getId())
                .title(community.getName())
                .description(community.getDescription())
                .searchType(SearchType.COMMUNITY)
                .relevanceScore(relevanceScore)
                .metadata(metadata)
                .build();
    }

    public SearchResultResponse toResponse(Event event, Double relevanceScore) {
        if (event == null) {
            return null;
        }
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("councilId", event.getCouncilId());
        metadata.put("organizerId", event.getOrganizerId());
        metadata.put("location", event.getLocation());
        metadata.put("startTime", event.getStartTime());
        metadata.put("endTime", event.getEndTime());

        return SearchResultResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .searchType(SearchType.EVENT)
                .relevanceScore(relevanceScore)
                .metadata(metadata)
                .build();
    }

    public SearchResultResponse toResponse(Resource resource, Double relevanceScore) {
        if (resource == null) {
            return null;
        }
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("uploaderId", resource.getUploaderId());
        metadata.put("councilId", resource.getCouncilId());
        metadata.put("communityId", resource.getCommunityId());
        metadata.put("tags", resource.getTags());
        metadata.put("fileType", resource.getFileType());
        metadata.put("fileSize", resource.getFileSize());
        metadata.put("downloadUrl", resource.getDownloadUrl());

        return SearchResultResponse.builder()
                .id(resource.getId())
                .title(resource.getTitle())
                .description(resource.getDescription())
                .searchType(SearchType.RESOURCE)
                .relevanceScore(relevanceScore)
                .metadata(metadata)
                .build();
    }
}
