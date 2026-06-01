package com.stark.steadyai.dto;

import java.time.LocalDateTime;

public record AiConversationSummaryDto(
        Long conversationId,
        String firstQuestion,
        LocalDateTime endedAt
) {
}
