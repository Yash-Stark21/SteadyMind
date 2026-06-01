package com.stark.steadyai.dto;

import com.stark.steadyai.enums.CompulsionDelayOutcome;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CompleteDelayAttemptRequest(
        @Min(value = 0, message = "Actual delay minutes cannot be negative")
        Integer actualDelayMinutes,

        @NotNull(message = "Outcome is required to complete the attempt")
        CompulsionDelayOutcome outcome
) {}
