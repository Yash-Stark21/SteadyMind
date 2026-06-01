package com.stark.steadyai.service;

import com.stark.steadyai.dto.AiCoachRequestDto;
import com.stark.steadyai.dto.AiCoachExchangeResponseDto;
import com.stark.steadyai.dto.AiConversationSummaryDto;
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
     * @return a response DTO containing the saved exchange and classification metadata
     */
    AiCoachExchangeResponseDto processMessage(AiCoachRequestDto requestDto, User user);

    /**
     * Retrieve the active AI Coach conversation messages for the given user.
     *
     * @param user the authenticated user
     * @return list of AiMessage entities ordered by createdAt ascending
     */
    List<AiMessage> getActiveConversationMessages(User user);

    /**
     * Retrieve messages from a conversation owned by the given user.
     *
     * @param conversationId the conversation id
     * @param user           the authenticated user
     * @return messages ordered by createdAt ascending
     */
    List<AiCoachExchangeResponseDto> getConversationMessages(Long conversationId, User user);

    /**
     * Retrieve closed AI Coach conversations for the given user.
     *
     * @param user the authenticated user
     * @return summaries ordered by endedAt descending
     */
    List<AiConversationSummaryDto> getRecentConversationSummaries(User user);

    /**
     * End the active conversation. The next message will start a new conversation.
     *
     * @param user the authenticated user
     * @return the closed conversation summary, or null when no active conversation exists
     */
    AiConversationSummaryDto startNewConversation(User user);
}
