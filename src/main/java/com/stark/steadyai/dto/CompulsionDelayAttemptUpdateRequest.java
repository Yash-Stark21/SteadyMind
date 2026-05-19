package com.stark.steadyai.dto;

import com.stark.steadyai.enums.CopingStrategy;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class CompulsionDelayAttemptUpdateRequest {

    @Size(max = 500, message = "Trigger description must be at most 500 characters")
    private String triggerDescription;

    @Positive(message = "Planned delay minutes must be positive")
    private Integer plannedDelayMinutes;

    private CopingStrategy copingStrategyUsed;

    private String notes;

    public String getTriggerDescription() {
        return triggerDescription;
    }

    public void setTriggerDescription(String triggerDescription) {
        this.triggerDescription = triggerDescription;
    }

    public Integer getPlannedDelayMinutes() {
        return plannedDelayMinutes;
    }

    public void setPlannedDelayMinutes(Integer plannedDelayMinutes) {
        this.plannedDelayMinutes = plannedDelayMinutes;
    }

    public CopingStrategy getCopingStrategyUsed() {
        return copingStrategyUsed;
    }

    public void setCopingStrategyUsed(CopingStrategy copingStrategyUsed) {
        this.copingStrategyUsed = copingStrategyUsed;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
