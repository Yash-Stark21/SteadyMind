package com.stark.steadyai.dto;

import com.stark.steadyai.enums.ExposureDifficulty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class ExposureTaskRequest {

    @NotBlank(message = "Title must not be blank")
    private String title;

    private String description;

    @NotNull(message = "Difficulty level must not be null")
    private ExposureDifficulty difficultyLevel;

    private LocalDateTime targetDate;

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

    public LocalDateTime getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDateTime targetDate) {
        this.targetDate = targetDate;
    }
}
