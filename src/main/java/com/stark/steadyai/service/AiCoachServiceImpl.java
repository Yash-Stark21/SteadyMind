package com.stark.steadyai.service;

import com.stark.steadyai.ai.AiClient;
import com.stark.steadyai.dto.AiCoachRequestDto;
import com.stark.steadyai.dto.AiCoachResponseDto;
import com.stark.steadyai.entity.AiMessage;
import com.stark.steadyai.entity.User;
import com.stark.steadyai.repository.AiMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of the AI Coach pipeline.
 *
 * Day 8 architecture:
 * 1. AiSafetyService pre-checks (crisis, reassurance-seeking)
 * 2. AiClient generates response (MockAiClient in mock-ai profile)
 * 3. AiPolicyService enforces final backend policy
 * 4. Audit record saved
 * 5. Safe response returned
 *
 * The AI model/client does not directly control the user-facing answer.
 * The backend policy layer decides what response is allowed.
 */
@Service
@Transactional
public class AiCoachServiceImpl implements AiCoachService {

    private final AiMessageRepository aiMessageRepository;
    private final AiClient aiClient;
    private final AiSafetyService aiSafetyService;
    private final AiPolicyService aiPolicyService;

    public AiCoachServiceImpl(AiMessageRepository aiMessageRepository,
                              AiClient aiClient,
                              AiSafetyService aiSafetyService,
                              AiPolicyService aiPolicyService) {
        this.aiMessageRepository = aiMessageRepository;
        this.aiClient = aiClient;
        this.aiSafetyService = aiSafetyService;
        this.aiPolicyService = aiPolicyService;
    }

    // =========================================================================
    // Public API
    // =========================================================================

    @Override
    public AiCoachResponseDto processMessage(AiCoachRequestDto requestDto, User user) {
        String userMessage = requestDto.getMessage().trim();

        AiCoachResponseDto responseDto;

        // Step 1: Safety pre-check — crisis / self-harm
        if (aiSafetyService.isCrisisMessage(userMessage)) {
            responseDto = aiSafetyService.buildCrisisResponse();
            responseDto.setUserMessage(userMessage);
            saveAuditRecord(user, userMessage, responseDto);
            return responseDto;
        }

        // Step 2: Safety pre-check — reassurance-seeking
        if (aiSafetyService.isReassuranceSeeking(userMessage)) {
            responseDto = aiSafetyService.buildReassuranceRedirectResponse();
            responseDto.setUserMessage(userMessage);
            saveAuditRecord(user, userMessage, responseDto);
            return responseDto;
        }

        // Step 3: Delegate to AI client
        responseDto = aiClient.generateResponse(requestDto);

        // Step 4: Apply backend policy enforcement
        responseDto = aiPolicyService.applyPolicy(responseDto);
        responseDto.setUserMessage(userMessage);

        // Step 5: Map userFacingMessage to aiResponse for backward compatibility
        if (responseDto.getUserFacingMessage() != null) {
            responseDto.setAiResponse(responseDto.getUserFacingMessage());
        }

        // Step 6: Save audit record
        saveAuditRecord(user, userMessage, responseDto);

        return responseDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiMessage> getRecentMessages(User user) {
        return aiMessageRepository.findByUserOrderByCreatedAtDesc(user);
    }

    // =========================================================================
    // Audit Persistence
    // =========================================================================

    /**
     * Saves an audit record of the AI Coach interaction.
     * Maps DTO fields to the AiMessage entity for persistence.
     */
    private void saveAuditRecord(User user, String userMessage, AiCoachResponseDto responseDto) {
        AiMessage aiMessage = new AiMessage();
        aiMessage.setUser(user);
        aiMessage.setUserMessage(userMessage);
        aiMessage.setIntent(responseDto.getIntent());
        aiMessage.setRiskLevel(responseDto.getRiskLevel());
        aiMessage.setResponseType(responseDto.getResponseType());
        aiMessage.setAiResponse(
                responseDto.getUserFacingMessage() != null
                        ? responseDto.getUserFacingMessage()
                        : responseDto.getAiResponse()
        );
        aiMessageRepository.save(aiMessage);
    }
}
