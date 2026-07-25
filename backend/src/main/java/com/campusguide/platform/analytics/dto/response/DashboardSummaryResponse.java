package com.campusguide.platform.analytics.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummaryResponse {
    private long totalUsers;
    private long activeUsers;
    private long totalRoadmaps;
    private long publishedRoadmaps;
    private long totalCommunities;
    private long activeCommunities;
    private long totalEvents;
    private long upcomingEvents;
    private long totalAiConversations;
    private long totalNotifications;
    private long totalResources;
    private LocalDateTime generatedAt;
}
