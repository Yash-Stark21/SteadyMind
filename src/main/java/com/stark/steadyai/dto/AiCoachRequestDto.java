package com.stark.steadyai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for the AI Coach message submission.
 * Contains the user's message with validation constraints.
 */
public class AiCoachRequestDto {

    @NotBlank(message = "Message cannot be empty")
    @Size(max = 1000, message = "Message must be under 1000 characters")
    private String message;

    public AiCoachRequestDto() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
