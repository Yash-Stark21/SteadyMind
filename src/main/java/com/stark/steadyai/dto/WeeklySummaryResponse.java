package com.stark.steadyai.dto;

import java.time.LocalDate;

public record WeeklySummaryResponse(
        LocalDate startDate,
        LocalDate endDate,
        int totalUrgeLogs,
        double averageIntensity,
        String mostCommonTrigger,
        String highestRiskPeriod,
        String progressObservations,
        String recurringPatterns,
        String suggestedNextSteps,
        String safetyNote
) {}
