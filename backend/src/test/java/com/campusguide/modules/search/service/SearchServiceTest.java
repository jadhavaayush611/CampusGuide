package com.campusguide.modules.search.service;

import com.campusguide.exception.BadRequestException;
import com.campusguide.exception.ResourceNotFoundException;
import com.campusguide.modules.course.entity.Course;
import com.campusguide.modules.course.repository.CourseRepository;
import com.campusguide.modules.roadmap.entity.Roadmap;
import com.campusguide.modules.roadmap.repository.RoadmapRepository;
import com.campusguide.modules.community.entity.Community;
import com.campusguide.modules.community.repository.CommunityRepository;
import com.campusguide.modules.event.entity.Event;
import com.campusguide.modules.event.repository.EventRepository;
import com.campusguide.modules.resource.entity.Resource;
import com.campusguide.modules.resource.repository.ResourceRepository;
import com.campusguide.modules.search.dto.request.GlobalSearchRequest;
import com.campusguide.modules.search.dto.response.GlobalSearchResponse;
import com.campusguide.modules.search.enums.SearchType;
import com.campusguide.modules.search.mapper.SearchMapper;
import com.campusguide.modules.search.service.impl.SearchServiceImpl;
import com.campusguide.modules.user.entity.User;
import com.campusguide.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private CourseRepository courseRepository;
    @Mock
    private RoadmapRepository roadmapRepository;
    @Mock
    private CommunityRepository communityRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private ResourceRepository resourceRepository;
    @Mock
    private UserRepository userRepository;

    @Spy
    private SearchMapper searchMapper = new SearchMapper();

    @InjectMocks
    private SearchServiceImpl searchService;

    private UserDetails userDetails;
    private User user;

    @BeforeEach
    void setUp() {
        userDetails = org.springframework.security.core.userdetails.User.withUsername("test@campusguide.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        user = User.builder()
                .id("user-123")
                .email("test@campusguide.com")
                .build();
    }

    @Test
    void search_AllModules_Success() {
        when(userRepository.findByEmail("test@campusguide.com")).thenReturn(Optional.of(user));

        Course course = Course.builder().id("c1").courseName("Advanced Java").courseCode("CS201").description("Java programming").active(true).build();
        Roadmap roadmap = Roadmap.builder().id("rm1").title("Java Roadmap").description("Become Java expert").isDeleted(false).build();
        Community community = Community.builder().id("cm1").name("Java Community").description("For Java lovers").isActive(true).build();
        Event event = Event.builder().id("e1").title("Java Conference").description("Annual conference").isDeleted(false).build();
        Resource resource = Resource.builder().id("res1").title("Java Book").description("Study resource").tags(List.of("Java")).isDeleted(false).build();

        when(courseRepository.findByActiveTrueAndCourseNameContainingIgnoreCaseOrActiveTrueAndCourseCodeContainingIgnoreCaseOrActiveTrueAndDescriptionContainingIgnoreCase(anyString(), anyString(), anyString()))
                .thenReturn(List.of(course));
        when(roadmapRepository.findByIsDeletedFalseAndTitleContainingIgnoreCaseOrIsDeletedFalseAndDescriptionContainingIgnoreCase(anyString(), anyString()))
                .thenReturn(List.of(roadmap));
        when(communityRepository.findByIsActiveTrueAndNameContainingIgnoreCaseOrIsActiveTrueAndDescriptionContainingIgnoreCase(anyString(), anyString()))
                .thenReturn(List.of(community));
        when(eventRepository.findByIsDeletedFalseAndTitleContainingIgnoreCaseOrIsDeletedFalseAndDescriptionContainingIgnoreCaseOrIsDeletedFalseAndLocationContainingIgnoreCaseOrderByStartTimeAsc(anyString(), anyString(), anyString()))
                .thenReturn(List.of(event));
        when(resourceRepository.findByIsDeletedFalseAndTitleContainingIgnoreCaseOrIsDeletedFalseAndDescriptionContainingIgnoreCaseOrIsDeletedFalseAndTagsContainingIgnoreCase(anyString(), anyString(), anyString()))
                .thenReturn(List.of(resource));

        GlobalSearchRequest request = GlobalSearchRequest.builder().query("Java").build();

        GlobalSearchResponse response = searchService.search(userDetails, request, Pageable.unpaged());

        assertNotNull(response);
        assertEquals("Java", response.getQuery());
        assertEquals(5, response.getTotalResults());
        assertEquals(5, response.getResults().size());
    }

    @Test
    void search_IndividualModule_Success() {
        when(userRepository.findByEmail("test@campusguide.com")).thenReturn(Optional.of(user));

        Course course = Course.builder().id("c1").courseName("Advanced Java").courseCode("CS201").description("Java programming").active(true).build();
        when(courseRepository.findByActiveTrueAndCourseNameContainingIgnoreCaseOrActiveTrueAndCourseCodeContainingIgnoreCaseOrActiveTrueAndDescriptionContainingIgnoreCase(anyString(), anyString(), anyString()))
                .thenReturn(List.of(course));

        GlobalSearchRequest request = GlobalSearchRequest.builder()
                .query("Java")
                .types(List.of(SearchType.COURSE))
                .build();

        GlobalSearchResponse response = searchService.search(userDetails, request, Pageable.unpaged());

        assertNotNull(response);
        assertEquals(1, response.getTotalResults());
        assertEquals(SearchType.COURSE, response.getResults().get(0).getSearchType());

        verifyNoInteractions(roadmapRepository, communityRepository, eventRepository, resourceRepository);
    }

    @Test
    void search_EmptyResultSet_Success() {
        when(userRepository.findByEmail("test@campusguide.com")).thenReturn(Optional.of(user));

        when(courseRepository.findByActiveTrueAndCourseNameContainingIgnoreCaseOrActiveTrueAndCourseCodeContainingIgnoreCaseOrActiveTrueAndDescriptionContainingIgnoreCase(anyString(), anyString(), anyString()))
                .thenReturn(Collections.emptyList());
        when(roadmapRepository.findByIsDeletedFalseAndTitleContainingIgnoreCaseOrIsDeletedFalseAndDescriptionContainingIgnoreCase(anyString(), anyString()))
                .thenReturn(Collections.emptyList());
        when(communityRepository.findByIsActiveTrueAndNameContainingIgnoreCaseOrIsActiveTrueAndDescriptionContainingIgnoreCase(anyString(), anyString()))
                .thenReturn(Collections.emptyList());
        when(eventRepository.findByIsDeletedFalseAndTitleContainingIgnoreCaseOrIsDeletedFalseAndDescriptionContainingIgnoreCaseOrIsDeletedFalseAndLocationContainingIgnoreCaseOrderByStartTimeAsc(anyString(), anyString(), anyString()))
                .thenReturn(Collections.emptyList());
        when(resourceRepository.findByIsDeletedFalseAndTitleContainingIgnoreCaseOrIsDeletedFalseAndDescriptionContainingIgnoreCaseOrIsDeletedFalseAndTagsContainingIgnoreCase(anyString(), anyString(), anyString()))
                .thenReturn(Collections.emptyList());

        GlobalSearchRequest request = GlobalSearchRequest.builder().query("NonExistent").build();

        GlobalSearchResponse response = searchService.search(userDetails, request, Pageable.unpaged());

        assertNotNull(response);
        assertEquals(0, response.getTotalResults());
        assertTrue(response.getResults().isEmpty());
    }

    @Test
    void search_BlankQuery_ThrowsBadRequestException() {
        GlobalSearchRequest request = GlobalSearchRequest.builder().query("   ").build();
        assertThrows(BadRequestException.class, () -> searchService.search(userDetails, request, Pageable.unpaged()));
    }

    @Test
    void search_RelevanceSorting_Success() {
        when(userRepository.findByEmail("test@campusguide.com")).thenReturn(Optional.of(user));

        Course courseExact = Course.builder().id("c1").courseName("Java").courseCode("CS201").description("Java programming").active(true).build();
        Roadmap roadmapContains = Roadmap.builder().id("rm1").title("Advanced Java Programming").description("Course description").isDeleted(false).build();
        Community communityDesc = Community.builder().id("cm1").name("Coding Club").description("A community for learning Java").isActive(true).build();

        when(courseRepository.findByActiveTrueAndCourseNameContainingIgnoreCaseOrActiveTrueAndCourseCodeContainingIgnoreCaseOrActiveTrueAndDescriptionContainingIgnoreCase(anyString(), anyString(), anyString()))
                .thenReturn(List.of(courseExact));
        when(roadmapRepository.findByIsDeletedFalseAndTitleContainingIgnoreCaseOrIsDeletedFalseAndDescriptionContainingIgnoreCase(anyString(), anyString()))
                .thenReturn(List.of(roadmapContains));
        when(communityRepository.findByIsActiveTrueAndNameContainingIgnoreCaseOrIsActiveTrueAndDescriptionContainingIgnoreCase(anyString(), anyString()))
                .thenReturn(List.of(communityDesc));
        when(eventRepository.findByIsDeletedFalseAndTitleContainingIgnoreCaseOrIsDeletedFalseAndDescriptionContainingIgnoreCaseOrIsDeletedFalseAndLocationContainingIgnoreCaseOrderByStartTimeAsc(anyString(), anyString(), anyString()))
                .thenReturn(Collections.emptyList());
        when(resourceRepository.findByIsDeletedFalseAndTitleContainingIgnoreCaseOrIsDeletedFalseAndDescriptionContainingIgnoreCaseOrIsDeletedFalseAndTagsContainingIgnoreCase(anyString(), anyString(), anyString()))
                .thenReturn(Collections.emptyList());

        GlobalSearchRequest request = GlobalSearchRequest.builder().query("Java").build();

        GlobalSearchResponse response = searchService.search(userDetails, request, Pageable.unpaged());

        assertNotNull(response);
        assertEquals(3, response.getResults().size());

        assertEquals("c1", response.getResults().get(0).getId());
        assertEquals(1.0, response.getResults().get(0).getRelevanceScore());

        assertEquals("rm1", response.getResults().get(1).getId());
        assertEquals(0.9, response.getResults().get(1).getRelevanceScore());

        assertEquals("cm1", response.getResults().get(2).getId());
        assertEquals(0.7, response.getResults().get(2).getRelevanceScore());
    }

    @Test
    void search_Pagination_Success() {
        when(userRepository.findByEmail("test@campusguide.com")).thenReturn(Optional.of(user));

        List<Course> courses = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            courses.add(Course.builder().id("c" + i).courseName("Java Course " + i).courseCode("CS" + i).description("Desc").active(true).build());
        }

        when(courseRepository.findByActiveTrueAndCourseNameContainingIgnoreCaseOrActiveTrueAndCourseCodeContainingIgnoreCaseOrActiveTrueAndDescriptionContainingIgnoreCase(anyString(), anyString(), anyString()))
                .thenReturn(courses);
        when(roadmapRepository.findByIsDeletedFalseAndTitleContainingIgnoreCaseOrIsDeletedFalseAndDescriptionContainingIgnoreCase(anyString(), anyString()))
                .thenReturn(Collections.emptyList());
        when(communityRepository.findByIsActiveTrueAndNameContainingIgnoreCaseOrIsActiveTrueAndDescriptionContainingIgnoreCase(anyString(), anyString()))
                .thenReturn(Collections.emptyList());
        when(eventRepository.findByIsDeletedFalseAndTitleContainingIgnoreCaseOrIsDeletedFalseAndDescriptionContainingIgnoreCaseOrIsDeletedFalseAndLocationContainingIgnoreCaseOrderByStartTimeAsc(anyString(), anyString(), anyString()))
                .thenReturn(Collections.emptyList());
        when(resourceRepository.findByIsDeletedFalseAndTitleContainingIgnoreCaseOrIsDeletedFalseAndDescriptionContainingIgnoreCaseOrIsDeletedFalseAndTagsContainingIgnoreCase(anyString(), anyString(), anyString()))
                .thenReturn(Collections.emptyList());

        GlobalSearchRequest request = GlobalSearchRequest.builder().query("Java").build();

        GlobalSearchResponse response = searchService.search(userDetails, request, PageRequest.of(1, 2));

        assertNotNull(response);
        assertEquals(5, response.getTotalResults());
        assertEquals(2, response.getResults().size());
    }
}
