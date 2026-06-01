package com.stark.steadyai.service;

import com.stark.steadyai.ai.AiClient;
import com.stark.steadyai.ai.AiResponseValidator;
import com.stark.steadyai.dto.AiCoachExchangeResponseDto;
import com.stark.steadyai.dto.AiCoachRequestDto;
import com.stark.steadyai.dto.AiCoachResponseDto;
import com.stark.steadyai.dto.AiConversationSummaryDto;
import com.stark.steadyai.entity.AiConversation;
import com.stark.steadyai.entity.AiMessage;
import com.stark.steadyai.entity.User;
import com.stark.steadyai.enums.CoachIntent;
import com.stark.steadyai.exception.ResourceNotFoundException;
import com.stark.steadyai.repository.AiConversationRepository;
import com.stark.steadyai.repository.AiMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * Implementation of the AI Coach pipeline.
 *
 * The AI model/client does not directly control the user-facing answer.
 * The backend policy layer decides what response is allowed.
 */
@Service
@Transactional
public class AiCoachServiceImpl implements AiCoachService {

    private static final Logger log = LoggerFactory.getLogger(AiCoachServiceImpl.class);
    private static final String MINIMAL_METADATA = "[MINIMAL METADATA]";

    private final AiMessageRepository aiMessageRepository;
    private final AiConversationRepository aiConversationRepository;
    private final AiClient aiClient;
    private final AiSafetyService aiSafetyService;
    private final AiPolicyService aiPolicyService;
    private final AiResponseValidator aiResponseValidator;

    public AiCoachServiceImpl(AiMessageRepository aiMessageRepository,
                              AiConversationRepository aiConversationRepository,
                              AiClient aiClient,
                              AiSafetyService aiSafetyService,
                              AiPolicyService aiPolicyService,
                              AiResponseValidator aiResponseValidator) {
        this.aiMessageRepository = aiMessageRepository;
        this.aiConversationRepository = aiConversationRepository;
        this.aiClient = aiClient;
        this.aiSafetyService = aiSafetyService;
        this.aiPolicyService = aiPolicyService;
        this.aiResponseValidator = aiResponseValidator;
    }

    @Override
    public AiCoachExchangeResponseDto processMessage(AiCoachRequestDto requestDto, User user) {
        String userMessage = extractAndValidateMessage(requestDto);

        AiCoachResponseDto responseDto;

        if (aiSafetyService.isCrisisOrSelfHarm(userMessage)) {
            responseDto = aiSafetyService.buildCrisisResponse();
            responseDto = aiPolicyService.applyPolicy(responseDto);
            return saveExchange(user, userMessage, responseDto);
        }

        if (aiSafetyService.isReassuranceSeeking(userMessage)) {
            responseDto = aiSafetyService.buildReassuranceRedirectResponse();
            responseDto = aiPolicyService.applyPolicy(responseDto);
            return saveExchange(user, userMessage, responseDto);
        }

        responseDto = generateAiResponseSafely(requestDto);
        responseDto = aiPolicyService.applyPolicy(responseDto);

        return saveExchange(user, userMessage, responseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiMessage> getActiveConversationMessages(User user) {
        return aiConversationRepository.findFirstByUserAndActiveTrueOrderByStartedAtDesc(user)
                .map(aiMessageRepository::findByConversationOrderByCreatedAtAsc)
                .orElse(Collections.emptyList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiCoachExchangeResponseDto> getConversationMessages(Long conversationId, User user) {
        AiConversation conversation = aiConversationRepository.findByIdAndUser(conversationId, user)
                .orElseThrow(() -> new ResourceNotFoundException("AI conversation not found."));

        return aiMessageRepository.findByConversationOrderByCreatedAtAsc(conversation).stream()
                .map(this::toExchangeResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiConversationSummaryDto> getRecentConversationSummaries(User user) {
        return aiConversationRepository.findByUserAndActiveFalseOrderByEndedAtDesc(user).stream()
                .map(this::toConversationSummary)
                .toList();
    }

    @Override
    public AiConversationSummaryDto startNewConversation(User user) {
        return aiConversationRepository.findFirstByUserAndActiveTrueOrderByStartedAtDesc(user)
                .map(conversation -> {
                    conversation.close();
                    AiConversation savedConversation = aiConversationRepository.save(conversation);
                    return toConversationSummary(savedConversation);
                })
                .orElse(null);
    }

    private AiCoachResponseDto generateAiResponseSafely(AiCoachRequestDto requestDto) {
        try {
            AiCoachResponseDto responseDto = aiClient.generateResponse(requestDto);

            if (aiResponseValidator.isInvalid(responseDto)) {
                log.warn("Invalid AI response received. Using fallback response.");
                return aiResponseValidator.buildFallback();
            }

            return responseDto;

        } catch (Exception e) {
            log.error("AI response generation failed. Using fallback response.", e);
            return aiResponseValidator.buildFallback();
        }
    }

    private String extractAndValidateMessage(AiCoachRequestDto requestDto) {
        if (requestDto == null ||
                requestDto.message() == null ||
                requestDto.message().isBlank()) {
            throw new IllegalArgumentException("AI coach message cannot be empty.");
        }

        return requestDto.message().trim();
    }

    private AiCoachExchangeResponseDto saveExchange(User user, String userMessage, AiCoachResponseDto responseDto) {
        String persistedUserMessage = toPersistedUserMessage(userMessage, responseDto);
        AiConversation conversation = getOrCreateActiveConversation(user, persistedUserMessage);

        AiMessage aiMessage = new AiMessage();
        aiMessage.setUser(user);
        aiMessage.setConversation(conversation);
        aiMessage.setUserMessage(persistedUserMessage);
        aiMessage.setIntent(responseDto.getIntent());
        aiMessage.setRiskLevel(responseDto.getRiskLevel());
        aiMessage.setResponseType(responseDto.getResponseType());
        aiMessage.setAiResponse(responseDto.getUserFacingMessage());

        AiMessage savedMessage = aiMessageRepository.save(aiMessage);
        conversation.touch();
        aiConversationRepository.save(conversation);

        return toExchangeResponse(savedMessage != null ? savedMessage : aiMessage);
    }

    private AiConversation getOrCreateActiveConversation(User user, String firstQuestion) {
        return aiConversationRepository.findFirstByUserAndActiveTrueOrderByStartedAtDesc(user)
                .orElseGet(() -> {
                    AiConversation conversation = new AiConversation();
                    conversation.setUser(user);
                    conversation.setFirstQuestion(firstQuestion);
                    conversation.setActive(true);
                    return aiConversationRepository.save(conversation);
                });
    }

    private String toPersistedUserMessage(String userMessage, AiCoachResponseDto responseDto) {
        if (responseDto.getIntent() == CoachIntent.CRISIS_OR_SELF_HARM) {
            return MINIMAL_METADATA;
        }

        return userMessage;
    }

    private AiCoachExchangeResponseDto toExchangeResponse(AiMessage message) {
        return new AiCoachExchangeResponseDto(
                message.getConversation().getId(),
                message.getUserMessage(),
                message.getAiResponse(),
                message.getIntent(),
                message.getRiskLevel(),
                message.getResponseType(),
                message.getCreatedAt()
        );
    }

    private AiConversationSummaryDto toConversationSummary(AiConversation conversation) {
        return new AiConversationSummaryDto(
                conversation.getId(),
                conversation.getFirstQuestion(),
                conversation.getEndedAt()
        );
    }
}
