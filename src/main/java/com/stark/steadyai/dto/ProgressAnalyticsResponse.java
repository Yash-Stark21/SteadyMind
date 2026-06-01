package com.stark.steadyai.dto;

import java.util.List;

public record ProgressAnalyticsResponse(
        int totalUrgeLogs,
        double averageUrgeIntensity,
        int highestUrgeIntensity,
        int totalExposureTasks,
        int completedExposureTasks,
        int pendingExposureTasks,
        int totalDelayAttempts,
        int completedDelayAttempts,
        int cancelledDelayAttempts,
        double averageDelayMinutes,
        List<TrendPointResponse> sevenDayUrgeTrend,
        List<TriggerBreakdownResponse> triggerBreakdown,
        List<IntensityDistributionResponse> intensityDistribution,
        List<String> progressObservations,
        String safetyNote
) {}
