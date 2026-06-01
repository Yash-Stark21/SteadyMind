package com.stark.steadyai.dto;

import com.stark.steadyai.enums.ExposureDifficulty;
import com.stark.steadyai.enums.ExposureStatus;

import java.time.LocalDateTime;

public record ExposureTaskResponse(
        Long id,
        String title,
        String description,
        ExposureDifficulty difficultyLevel,
        ExposureStatus status,
        LocalDateTime targetDate,
        LocalDateTime completedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
