package com.campusguide.modules.analytics.service.impl;

import com.campusguide.modules.analytics.dto.response.DashboardSummaryResponse;
import com.campusguide.modules.analytics.dto.response.ModuleStatisticsResponse;
import com.campusguide.modules.analytics.service.interfaces.AnalyticsService;
import com.campusguide.modules.user.repository.UserRepository;
import com.campusguide.modules.roadmap.repository.RoadmapRepository;
import com.campusguide.modules.community.repository.CommunityRepository;
import com.campusguide.modules.event.repository.EventRepository;
import com.campusguide.modules.ai.repository.ConversationRepository;
import com.campusguide.modules.notification.repository.NotificationRepository;
import com.campusguide.modules.resource.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsServiceImpl.class);

    private final UserRepository userRepository;
    private final RoadmapRepository roadmapRepository;
    private final CommunityRepository communityRepository;
    private final EventRepository eventRepository;
    private final ConversationRepository conversationRepository;
    private final NotificationRepository notificationRepository;
    private final ResourceRepository resourceRepository;

    @Override
    public DashboardSummaryResponse getDashboardSummary() {
        logger.info("Generating admin dashboard summary");
        long startTime = System.currentTimeMillis();

        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByIsVerifiedTrue();

        long totalRoadmaps = roadmapRepository.count();
        long publishedRoadmaps = roadmapRepository.countByIsDeletedFalse();

        long totalCommunities = communityRepository.count();
        long activeCommunities = communityRepository.countByIsActiveTrue();

        long totalEvents = eventRepository.countByIsDeletedFalse();
        long upcomingEvents = eventRepository.countByIsDeletedFalseAndIsCancelledFalseAndStartTimeGreaterThanEqual(LocalDateTime.now());

        long totalAiConversations = conversationRepository.count();
        long totalNotifications = notificationRepository.count();
        long totalResources = resourceRepository.countByIsDeletedFalse();

        long duration = System.currentTimeMillis() - startTime;
        logger.info("Admin dashboard summary generated in {} ms", duration);

        return DashboardSummaryResponse.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .totalRoadmaps(totalRoadmaps)
                .publishedRoadmaps(publishedRoadmaps)
                .totalCommunities(totalCommunities)
                .activeCommunities(activeCommunities)
                .totalEvents(totalEvents)
                .upcomingEvents(upcomingEvents)
                .totalAiConversations(totalAiConversations)
                .totalNotifications(totalNotifications)
                .totalResources(totalResources)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    @Override
    public ModuleStatisticsResponse getUserStatistics() {
        logger.info("Generating user statistics");
        long startTime = System.currentTimeMillis();

        long total = userRepository.count();
        long active = userRepository.countByIsVerifiedTrue();

        long duration = System.currentTimeMillis() - startTime;
        logger.info("User statistics generated in {} ms", duration);

        return ModuleStatisticsResponse.builder()
                .total(total)
                .active(active)
                .archived(0L)
                .build();
    }

    @Override
    public ModuleStatisticsResponse getEventStatistics() {
        logger.info("Generating event statistics");
        long startTime = System.currentTimeMillis();

        long total = eventRepository.countByIsDeletedFalse();
        long active = eventRepository.countByIsDeletedFalseAndIsCancelledFalseAndStartTimeGreaterThanEqual(LocalDateTime.now());

        long duration = System.currentTimeMillis() - startTime;
        logger.info("Event statistics generated in {} ms", duration);

        return ModuleStatisticsResponse.builder()
                .total(total)
                .active(active)
                .archived(0L)
                .build();
    }

    @Override
    public ModuleStatisticsResponse getCommunityStatistics() {
        logger.info("Generating community statistics");
        long startTime = System.currentTimeMillis();

        long total = communityRepository.count();
        long active = communityRepository.countByIsActiveTrue();

        long duration = System.currentTimeMillis() - startTime;
        logger.info("Community statistics generated in {} ms", duration);

        return ModuleStatisticsResponse.builder()
                .total(total)
                .active(active)
                .archived(0L)
                .build();
    }
}
