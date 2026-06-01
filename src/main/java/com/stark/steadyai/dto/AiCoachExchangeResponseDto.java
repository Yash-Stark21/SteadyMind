package com.stark.steadyai.dto;

import com.stark.steadyai.enums.CoachIntent;
import com.stark.steadyai.enums.ResponseType;
import com.stark.steadyai.enums.RiskLevel;

import java.time.LocalDateTime;

public record AiCoachExchangeResponseDto(
        Long conversationId,
        String userMessage,
        String aiResponse,
        CoachIntent intent,
        RiskLevel riskLevel,
        ResponseType responseType,
        LocalDateTime createdAt
) {
}
