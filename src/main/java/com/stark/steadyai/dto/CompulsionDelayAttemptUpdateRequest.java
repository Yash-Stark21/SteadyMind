package com.stark.steadyai.dto;

import com.stark.steadyai.enums.CopingStrategy;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CompulsionDelayAttemptUpdateRequest(
        @Size(max = 500, message = "Trigger description must be at most 500 characters")
        String triggerDescription,

        @Positive(message = "Planned delay minutes must be positive")
        Integer plannedDelayMinutes,

        CopingStrategy copingStrategyUsed,

        String notes
) {}
