package com.stark.steadyai.service;

import com.stark.steadyai.dto.AiCoachRequestDto;
import com.stark.steadyai.dto.AiCoachResponseDto;
import com.stark.steadyai.entity.AiMessage;
import com.stark.steadyai.entity.User;

import java.util.List;

/**
 * Service interface for the controlled AI Coach pipeline.
 * Handles intent detection, risk classification, response generation,
 * and audit record persistence — all through rule-based logic.
 */
public interface AiCoachService {

    /**
     * Process a user message through the controlled AI Coach pipeline:
     * Intent detection → Risk classification → Response type selection → Response generation → Audit save.
     *
     * @param requestDto the user's message wrapped in a validated DTO
     * @param user       the authenticated user
     * @return a response DTO containing the AI response and all classification metadata
     */
    AiCoachResponseDto processMessage(AiCoachRequestDto requestDto, User user);

    /**
     * Retrieve recent AI Coach messages for the given user, ordered newest first.
     *
     * @param user the authenticated user
     * @return list of AiMessage entities ordered by createdAt descending
     */
    List<AiMessage> getRecentMessages(User user);
}
