package com.stark.steadyai.dto;

import java.time.LocalDateTime;

public class UrgeLogResponse {

    private Long id;
    private String triggerText;
    private String obsessionText;
    private String compulsionUrge;
    private Integer intensityBefore;
    private Integer delayMinutes;
    private Integer intensityAfter;
    private Boolean compulsionPerformed;
    private LocalDateTime createdAt;

    public UrgeLogResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
