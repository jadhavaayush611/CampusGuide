package com.campusguide.platform.analytics.service;

import com.campusguide.platform.analytics.dto.response.DashboardSummaryResponse;
import com.campusguide.platform.analytics.dto.response.ModuleStatisticsResponse;
import com.campusguide.platform.analytics.service.impl.AnalyticsServiceImpl;
import com.campusguide.platform.user.repository.UserRepository;
import com.campusguide.campus.academic.roadmap.repository.RoadmapRepository;
import com.campusguide.campus.community.repository.CommunityRepository;
import com.campusguide.campus.event.repository.EventRepository;
import com.campusguide.personal.ai.repository.ConversationRepository;
import com.campusguide.personal.notification.repository.NotificationRepository;
import com.campusguide.campus.resource.repository.ResourceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoadmapRepository roadmapRepository;

    @Mock
    private CommunityRepository communityRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    @Test
    void testGetDashboardSummary_EmptyDatabase() {
        // Arrange
        when(userRepository.count()).thenReturn(0L);
        when(roadmapRepository.count()).thenReturn(0L);
        when(roadmapRepository.countByIsDeletedFalse()).thenReturn(0L);
        when(communityRepository.count()).thenReturn(0L);
        when(communityRepository.countByIsActiveTrue()).thenReturn(0L);
        when(eventRepository.countByIsDeletedFalse()).thenReturn(0L);
        when(eventRepository.countByIsDeletedFalseAndIsCancelledFalseAndStartTimeGreaterThanEqual(any(LocalDateTime.class))).thenReturn(0L);
        when(conversationRepository.count()).thenReturn(0L);
        when(notificationRepository.count()).thenReturn(0L);
        when(resourceRepository.countByIsDeletedFalse()).thenReturn(0L);

        // Act
        DashboardSummaryResponse response = analyticsService.getDashboardSummary();

        // Assert
        assertNotNull(response);
        assertEquals(0, response.getTotalUsers());
        assertEquals(0, response.getActiveUsers());
        assertEquals(0, response.getTotalRoadmaps());
        assertEquals(0, response.getPublishedRoadmaps());
        assertEquals(0, response.getTotalCommunities());
        assertEquals(0, response.getActiveCommunities());
        assertEquals(0, response.getTotalEvents());
        assertEquals(0, response.getUpcomingEvents());
        assertEquals(0, response.getTotalAiConversations());
        assertEquals(0, response.getTotalNotifications());
        assertEquals(0, response.getTotalResources());
        assertNotNull(response.getGeneratedAt());

        // Verify repository interaction
        verify(userRepository, times(1)).count();
        verify(roadmapRepository, times(1)).count();
        verify(roadmapRepository, times(1)).countByIsDeletedFalse();
        verify(communityRepository, times(1)).count();
        verify(communityRepository, times(1)).countByIsActiveTrue();
        verify(eventRepository, times(1)).countByIsDeletedFalse();
        verify(eventRepository, times(1)).countByIsDeletedFalseAndIsCancelledFalseAndStartTimeGreaterThanEqual(any(LocalDateTime.class));
        verify(conversationRepository, times(1)).count();
        verify(notificationRepository, times(1)).count();
        verify(resourceRepository, times(1)).countByIsDeletedFalse();
    }

    @Test
    void testGetDashboardSummary_PopulatedData() {
        // Arrange
        when(userRepository.count()).thenReturn(100L);
        when(roadmapRepository.count()).thenReturn(50L);
        when(roadmapRepository.countByIsDeletedFalse()).thenReturn(45L);
        when(communityRepository.count()).thenReturn(20L);
        when(communityRepository.countByIsActiveTrue()).thenReturn(18L);
        when(eventRepository.countByIsDeletedFalse()).thenReturn(30L);
        when(eventRepository.countByIsDeletedFalseAndIsCancelledFalseAndStartTimeGreaterThanEqual(any(LocalDateTime.class))).thenReturn(5L);
        when(conversationRepository.count()).thenReturn(200L);
        when(notificationRepository.count()).thenReturn(1000L);
        when(resourceRepository.countByIsDeletedFalse()).thenReturn(150L);

        // Act
        DashboardSummaryResponse response = analyticsService.getDashboardSummary();

        // Assert
        assertNotNull(response);
        assertEquals(100, response.getTotalUsers());
        assertEquals(100, response.getActiveUsers());
        assertEquals(50, response.getTotalRoadmaps());
        assertEquals(45, response.getPublishedRoadmaps());
        assertEquals(20, response.getTotalCommunities());
        assertEquals(18, response.getActiveCommunities());
        assertEquals(30, response.getTotalEvents());
        assertEquals(5, response.getUpcomingEvents());
        assertEquals(200, response.getTotalAiConversations());
        assertEquals(1000, response.getTotalNotifications());
        assertEquals(150, response.getTotalResources());
        assertNotNull(response.getGeneratedAt());

        // Verify repository interactions
        verify(userRepository, times(1)).count();
        verify(roadmapRepository, times(1)).count();
        verify(roadmapRepository, times(1)).countByIsDeletedFalse();
        verify(communityRepository, times(1)).count();
        verify(communityRepository, times(1)).countByIsActiveTrue();
        verify(eventRepository, times(1)).countByIsDeletedFalse();
        verify(eventRepository, times(1)).countByIsDeletedFalseAndIsCancelledFalseAndStartTimeGreaterThanEqual(any(LocalDateTime.class));
        verify(conversationRepository, times(1)).count();
        verify(notificationRepository, times(1)).count();
        verify(resourceRepository, times(1)).countByIsDeletedFalse();
    }

    @Test
    void testGetUserStatistics() {
        when(userRepository.count()).thenReturn(100L);

        ModuleStatisticsResponse stats = analyticsService.getUserStatistics();

        assertEquals(100L, stats.getTotal());
        assertEquals(100L, stats.getActive());
        verify(userRepository, times(1)).count();
    }

    @Test
    void testGetEventStatistics() {
        when(eventRepository.countByIsDeletedFalse()).thenReturn(30L);
        when(eventRepository.countByIsDeletedFalseAndIsCancelledFalseAndStartTimeGreaterThanEqual(any(LocalDateTime.class))).thenReturn(5L);

        ModuleStatisticsResponse stats = analyticsService.getEventStatistics();

        assertEquals(30L, stats.getTotal());
        assertEquals(5L, stats.getActive());
        verify(eventRepository, times(1)).countByIsDeletedFalse();
        verify(eventRepository, times(1)).countByIsDeletedFalseAndIsCancelledFalseAndStartTimeGreaterThanEqual(any(LocalDateTime.class));
    }

    @Test
    void testGetCommunityStatistics() {
        when(communityRepository.count()).thenReturn(20L);
        when(communityRepository.countByIsActiveTrue()).thenReturn(18L);

        ModuleStatisticsResponse stats = analyticsService.getCommunityStatistics();

        assertEquals(20L, stats.getTotal());
        assertEquals(18L, stats.getActive());
        verify(communityRepository, times(1)).count();
        verify(communityRepository, times(1)).countByIsActiveTrue();
    }
}
