package com.stark.steadyai.dto;

import com.stark.steadyai.enums.CompulsionDelayOutcome;
import com.stark.steadyai.enums.CopingStrategy;

import java.time.LocalDateTime;

public record CompulsionDelayAttemptResponse(
        Long id,
        Long urgeLogId,
        Long exposureTaskId,
        String triggerDescription,
        Integer plannedDelayMinutes,
        Integer actualDelayMinutes,
        CompulsionDelayOutcome outcome,
        CopingStrategy copingStrategyUsed,
        String notes,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
