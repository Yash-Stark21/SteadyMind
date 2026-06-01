package com.stark.steadyai.dto;

import java.time.LocalDateTime;

public record UrgeLogResponse(
        Long id,
        String triggerText,
        String obsessionText,
        String compulsionUrge,
        Integer intensityBefore,
        Integer delayMinutes,
        Integer intensityAfter,
        Boolean compulsionPerformed,
        LocalDateTime createdAt
) {}
