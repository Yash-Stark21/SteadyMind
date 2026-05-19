package com.stark.steadyai.dto;

import com.stark.steadyai.enums.CompulsionDelayOutcome;
import com.stark.steadyai.enums.CopingStrategy;

import java.time.LocalDateTime;

public class CompulsionDelayAttemptResponse {

    private Long id;
    private Long urgeLogId;
    private Long exposureTaskId;
    private String triggerDescription;
    private Integer plannedDelayMinutes;
    private Integer actualDelayMinutes;
    private CompulsionDelayOutcome outcome;
    private CopingStrategy copingStrategyUsed;
    private String notes;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUrgeLogId() {
        return urgeLogId;
    }

    public void setUrgeLogId(Long urgeLogId) {
        this.urgeLogId = urgeLogId;
    }

    public Long getExposureTaskId() {
        return exposureTaskId;
    }

    public void setExposureTaskId(Long exposureTaskId) {
        this.exposureTaskId = exposureTaskId;
    }

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

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
