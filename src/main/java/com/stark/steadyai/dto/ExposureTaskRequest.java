package com.stark.steadyai.dto;

import com.stark.steadyai.enums.ExposureDifficulty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ExposureTaskRequest(
        @NotBlank(message = "Title must not be blank")
        String title,

        String description,

        @NotNull(message = "Difficulty level must not be null")
        ExposureDifficulty difficultyLevel,

        LocalDateTime targetDate
) {}
