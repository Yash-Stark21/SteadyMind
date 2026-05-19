package com.stark.steadyai.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Arrays;
import java.util.List;

public class UrgeLogRequest {

    @NotBlank(message = "Trigger text is required")
    @Size(max = 500, message = "Trigger text must not exceed 500 characters")
    private String triggerText;

    @Size(max = 500, message = "Obsession text must not exceed 500 characters")
    private String obsessionText;

    @NotBlank(message = "Compulsion urge is required")
    @Size(max = 300, message = "Compulsion urge must not exceed 300 characters")
    private String compulsionUrge;

    @NotNull(message = "Intensity before is required")
    @Min(value = 1, message = "Intensity before must be at least 1")
    @Max(value = 10, message = "Intensity before must be at most 10")
    private Integer intensityBefore;

    @NotNull(message = "Delay minutes is required")
    private Integer delayMinutes;

    @Min(value = 1, message = "Intensity after must be at least 1")
    @Max(value = 10, message = "Intensity after must be at most 10")
    private Integer intensityAfter;

    @NotNull(message = "Compulsion performed is required")
    private Boolean compulsionPerformed;

    @AssertTrue(message = "Delay minutes must be one of: 0, 2, 5, 10, 15")
    public boolean isValidDelayMinutes() {
        if (delayMinutes == null) return true; // Handled by @NotNull
        List<Integer> allowedValues = Arrays.asList(0, 2, 5, 10, 15);
        return allowedValues.contains(delayMinutes);
    }

    public UrgeLogRequest() {
    }

    public String getTriggerText() {
        return triggerText;
    }

    public void setTriggerText(String triggerText) {
        this.triggerText = triggerText;
    }

    public String getObsessionText() {
        return obsessionText;
    }

    public void setObsessionText(String obsessionText) {
        this.obsessionText = obsessionText;
    }

    public String getCompulsionUrge() {
        return compulsionUrge;
    }

    public void setCompulsionUrge(String compulsionUrge) {
        this.compulsionUrge = compulsionUrge;
    }

    public Integer getIntensityBefore() {
        return intensityBefore;
    }

    public void setIntensityBefore(Integer intensityBefore) {
        this.intensityBefore = intensityBefore;
    }

    public Integer getDelayMinutes() {
        return delayMinutes;
    }

    public void setDelayMinutes(Integer delayMinutes) {
        this.delayMinutes = delayMinutes;
    }

    public Integer getIntensityAfter() {
        return intensityAfter;
    }

    public void setIntensityAfter(Integer intensityAfter) {
        this.intensityAfter = intensityAfter;
    }

    public Boolean getCompulsionPerformed() {
        return compulsionPerformed;
    }

    public void setCompulsionPerformed(Boolean compulsionPerformed) {
        this.compulsionPerformed = compulsionPerformed;
    }
}
