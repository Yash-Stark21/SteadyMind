package com.stark.steadyai.ai;

import com.stark.steadyai.dto.AiCoachRequestDto;
import com.stark.steadyai.dto.AiCoachResponseDto;

/**
 * Abstraction for AI response generation.
 *
 * Today: MockAiClient provides deterministic, keyword-based responses.
 * Later: A real implementation (e.g., SpringAiClient) can integrate with
 * OpenAI or another LLM provider without changing the service layer.
 *
 * The AiCoachService never calls an AI provider directly — it always
 * goes through this interface.
 */
public interface AiClient {

    /**
     * Generate a structured coaching response for the given user message.
     *
     * @param requestDto the validated user message
     * @return a fully populated response DTO with intent, risk, response type, and message
     */
    AiCoachResponseDto generateResponse(AiCoachRequestDto requestDto);
}
