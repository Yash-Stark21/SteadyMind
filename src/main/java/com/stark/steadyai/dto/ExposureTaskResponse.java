package com.stark.steadyai.dto;

import com.stark.steadyai.enums.ExposureDifficulty;
import com.stark.steadyai.enums.ExposureStatus;

import java.time.LocalDateTime;

public class ExposureTaskResponse {

    private Long id;
    private String title;
    private String description;
    private ExposureDifficulty difficultyLevel;
    private ExposureStatus status;
    private LocalDateTime targetDate;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ExposureDifficulty getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(ExposureDifficulty difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public ExposureStatus getStatus() {
        return status;
    }

    public void setStatus(ExposureStatus status) {
        this.status = status;
    }

    public LocalDateTime getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDateTime targetDate) {
        this.targetDate = targetDate;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
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
