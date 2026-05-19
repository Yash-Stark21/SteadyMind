package com.stark.steadyai.dto;

import com.stark.steadyai.enums.CompulsionDelayOutcome;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CompleteDelayAttemptRequest {

    @Min(value = 0, message = "Actual delay minutes cannot be negative")
    private Integer actualDelayMinutes;

    @NotNull(message = "Outcome is required to complete the attempt")
    private CompulsionDelayOutcome outcome;

    public Integer getActualDelayMinutes() {
        return actualDelayMinutes;
    }

    public void setActualDelayMinutes(Integer actualDelayMinutes) {
        this.actualDelayMinutes = actualDelayMinutes;
    }

    public CompulsionDelayOutcome getOutcome() {
        return outcome;
    }

    public void setOutcome(CompulsionDelayOutcome outcome) {
        this.outcome = outcome;
    }
}
