package com.stark.steadyai.dto;

public record TherapistPatientSummaryDto(
        Long patientId,
        String name,
        String email,
        int totalUrgeLogs,
        int totalExposureTasks,
        long completedExposureTasks,
        int totalDelayAttempts,
        int totalAiConversations
) {
}
