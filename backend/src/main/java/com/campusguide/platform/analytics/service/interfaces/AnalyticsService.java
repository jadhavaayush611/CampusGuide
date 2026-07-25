package com.campusguide.platform.analytics.service.interfaces;

import com.campusguide.platform.analytics.dto.response.DashboardSummaryResponse;
import com.campusguide.platform.analytics.dto.response.ModuleStatisticsResponse;

public interface AnalyticsService {
    DashboardSummaryResponse getDashboardSummary();
    ModuleStatisticsResponse getUserStatistics();
    ModuleStatisticsResponse getEventStatistics();
    ModuleStatisticsResponse getCommunityStatistics();
}
