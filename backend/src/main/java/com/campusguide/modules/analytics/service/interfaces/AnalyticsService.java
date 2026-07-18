package com.campusguide.modules.analytics.service.interfaces;

import com.campusguide.modules.analytics.dto.response.DashboardSummaryResponse;
import com.campusguide.modules.analytics.dto.response.ModuleStatisticsResponse;

public interface AnalyticsService {
    DashboardSummaryResponse getDashboardSummary();
    ModuleStatisticsResponse getUserStatistics();
    ModuleStatisticsResponse getEventStatistics();
    ModuleStatisticsResponse getCommunityStatistics();
}
