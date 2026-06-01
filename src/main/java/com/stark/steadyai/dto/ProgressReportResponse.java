package com.stark.steadyai.dto;

import java.util.List;

public record ProgressReportResponse(
        String generatedAt,
        String startDate,
        String endDate,
        String userDisplayName,
        int totalUrgeLogs,
        double averageUrgeIntensity,
        String mostCommonTrigger,
        int completedExposureTasks,
        int pendingExposureTasks,
        int completedDelayAttempts,
        double averageDelayMinutes,
        String weeklySummary,
        List<String> keyObservations,
        List<String> suggestedNextSteps,
        String safetyNote
) {}
