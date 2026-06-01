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
import com.stark.steadyai.enums.ResponseType;
import com.stark.steadyai.enums.RiskLevel;
import com.stark.steadyai.enums.SuggestedAction;
import com.stark.steadyai.exception.ResourceNotFoundException;
import com.stark.steadyai.repository.AiConversationRepository;
import com.stark.steadyai.repository.AiMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiCoachServiceTest {

    @Mock
    private AiMessageRepository aiMessageRepository;

    @Mock
    private AiConversationRepository aiConversationRepository;

    @Mock
    private AiClient aiClient;

    @Mock
    private AiSafetyService aiSafetyService;

    @Mock
    private AiPolicyService aiPolicyService;

    @Mock
    private AiResponseValidator aiResponseValidator;

    private AiCoachServiceImpl aiCoachService;
    private User user;
    private AtomicLong conversationIds;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1L);
        conversationIds = new AtomicLong(10L);

        when(aiConversationRepository.findFirstByUserAndActiveTrueOrderByStartedAtDesc(user))
                .thenReturn(Optional.empty());
        when(aiConversationRepository.save(any(AiConversation.class))).thenAnswer(invocation -> {
            AiConversation conversation = invocation.getArgument(0);
            if (conversation.getId() == null) {
                conversation.setId(conversationIds.getAndIncrement());
            }
            return conversation;
        });
        when(aiMessageRepository.save(any(AiMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(aiPolicyService.applyPolicy(any())).thenAnswer(invocation -> invocation.getArgument(0));

        aiCoachService = new AiCoachServiceImpl(
                aiMessageRepository,
                aiConversationRepository,
                aiClient,
                aiSafetyService,
                aiPolicyService,
                aiResponseValidator
        );
    }

    @Test
    void aiClientFailureReturnsSafeFallbackExchange() {
        AiCoachRequestDto requestDto = new AiCoachRequestDto("I am anxious");
        AiCoachResponseDto fallbackResponse = response(
                CoachIntent.REASSURANCE_SEEKING,
                RiskLevel.LOW,
                ResponseType.ERP_REDIRECT,
                false,
                "I cannot verify or disprove the fear for you",
                SuggestedAction.START_DELAY_TIMER
        );

        when(aiSafetyService.isCrisisOrSelfHarm(any())).thenReturn(false);
        when(aiSafetyService.isReassuranceSeeking(any())).thenReturn(false);
        when(aiClient.generateResponse(any())).thenThrow(new RuntimeException("API down"));
        when(aiResponseValidator.buildFallback()).thenReturn(fallbackResponse);

        AiCoachExchangeResponseDto result = aiCoachService.processMessage(requestDto, user);

        assertThat(result.responseType()).isEqualTo(ResponseType.ERP_REDIRECT);
        assertThat(result.aiResponse()).contains("I cannot verify or disprove");
        verify(aiMessageRepository).save(any(AiMessage.class));
    }

    @Test
    void invalidAiResponseReturnsSafeFallbackExchange() {
        AiCoachRequestDto requestDto = new AiCoachRequestDto("I am anxious");
        AiCoachResponseDto invalidResponse = new AiCoachResponseDto();
        AiCoachResponseDto fallbackResponse = response(
                CoachIntent.REASSURANCE_SEEKING,
                RiskLevel.LOW,
                ResponseType.ERP_REDIRECT,
                false,
                "I cannot verify or disprove the fear for you",
                SuggestedAction.START_DELAY_TIMER
        );

        when(aiSafetyService.isCrisisOrSelfHarm(any())).thenReturn(false);
        when(aiSafetyService.isReassuranceSeeking(any())).thenReturn(false);
        when(aiClient.generateResponse(any())).thenReturn(invalidResponse);
        when(aiResponseValidator.isInvalid(invalidResponse)).thenReturn(true);
        when(aiResponseValidator.buildFallback()).thenReturn(fallbackResponse);

        AiCoachExchangeResponseDto result = aiCoachService.processMessage(requestDto, user);

        assertThat(result.responseType()).isEqualTo(ResponseType.ERP_REDIRECT);
        assertThat(result.aiResponse()).contains("I cannot verify or disprove");
        verify(aiMessageRepository).save(any(AiMessage.class));
    }

    @Test
    void reassuranceSeekingPreCheckBypassesAiClient() {
        AiCoachRequestDto requestDto = new AiCoachRequestDto("Can you promise?");
        AiCoachResponseDto redirectDto = response(
                CoachIntent.REASSURANCE_SEEKING,
                RiskLevel.LOW,
                ResponseType.ERP_REDIRECT,
                false,
                "Sit with uncertainty.",
                SuggestedAction.START_DELAY_TIMER
        );

        when(aiSafetyService.isCrisisOrSelfHarm(any())).thenReturn(false);
        when(aiSafetyService.isReassuranceSeeking(any())).thenReturn(true);
        when(aiSafetyService.buildReassuranceRedirectResponse()).thenReturn(redirectDto);

        aiCoachService.processMessage(requestDto, user);

        verify(aiClient, never()).generateResponse(any());
        verify(aiMessageRepository).save(any(AiMessage.class));
    }

    @Test
    void crisisPreCheckStoresMinimalMetadataForMessageAndConversationFirstQuestion() {
        AiCoachRequestDto requestDto = new AiCoachRequestDto("I want to kill myself");
        AiCoachResponseDto crisisDto = response(
                CoachIntent.CRISIS_OR_SELF_HARM,
                RiskLevel.HIGH,
                ResponseType.CRISIS_STATIC_MESSAGE,
                false,
                "Contact local emergency services now.",
                SuggestedAction.CONTACT_SUPPORT
        );

        when(aiSafetyService.isCrisisOrSelfHarm(any())).thenReturn(true);
        when(aiSafetyService.buildCrisisResponse()).thenReturn(crisisDto);

        aiCoachService.processMessage(requestDto, user);

        verify(aiClient, never()).generateResponse(any());

        ArgumentCaptor<AiMessage> messageCaptor = ArgumentCaptor.forClass(AiMessage.class);
        verify(aiMessageRepository).save(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getUserMessage()).isEqualTo("[MINIMAL METADATA]");

        ArgumentCaptor<AiConversation> conversationCaptor = ArgumentCaptor.forClass(AiConversation.class);
        verify(aiConversationRepository, org.mockito.Mockito.atLeastOnce()).save(conversationCaptor.capture());
        assertThat(conversationCaptor.getAllValues().get(0).getFirstQuestion()).isEqualTo("[MINIMAL METADATA]");
    }

    @Test
    void validAiResponseIsSavedToActiveConversation() {
        AiCoachRequestDto requestDto = new AiCoachRequestDto("Normal message");
        AiCoachResponseDto validResponse = response(
                CoachIntent.URGE_SUPPORT,
                RiskLevel.LOW,
                ResponseType.REFLECTION_PROMPT,
                true,
                "Great job",
                SuggestedAction.START_DELAY_TIMER
        );

        when(aiSafetyService.isCrisisOrSelfHarm(any())).thenReturn(false);
        when(aiSafetyService.isReassuranceSeeking(any())).thenReturn(false);
        when(aiClient.generateResponse(any())).thenReturn(validResponse);

        AiCoachExchangeResponseDto result = aiCoachService.processMessage(requestDto, user);

        ArgumentCaptor<AiMessage> captor = ArgumentCaptor.forClass(AiMessage.class);
        verify(aiMessageRepository).save(captor.capture());

        assertThat(captor.getValue().getAiResponse()).isEqualTo("Great job");
        assertThat(captor.getValue().getConversation()).isNotNull();
        assertThat(result.userMessage()).isEqualTo("Normal message");
        assertThat(result.aiResponse()).isEqualTo("Great job");
    }

    @Test
    void secondMessageReusesExistingActiveConversation() {
        AiConversation existingConversation = conversation(42L, "First question", true);
        AiCoachRequestDto requestDto = new AiCoachRequestDto("Second message");
        AiCoachResponseDto validResponse = response(
                CoachIntent.URGE_SUPPORT,
                RiskLevel.LOW,
                ResponseType.REFLECTION_PROMPT,
                true,
                "Second response",
                SuggestedAction.START_DELAY_TIMER
        );

        when(aiConversationRepository.findFirstByUserAndActiveTrueOrderByStartedAtDesc(user))
                .thenReturn(Optional.of(existingConversation));
        when(aiSafetyService.isCrisisOrSelfHarm(any())).thenReturn(false);
        when(aiSafetyService.isReassuranceSeeking(any())).thenReturn(false);
        when(aiClient.generateResponse(any())).thenReturn(validResponse);

        aiCoachService.processMessage(requestDto, user);

        ArgumentCaptor<AiMessage> captor = ArgumentCaptor.forClass(AiMessage.class);
        verify(aiMessageRepository).save(captor.capture());
        assertThat(captor.getValue().getConversation()).isSameAs(existingConversation);
        assertThat(existingConversation.getFirstQuestion()).isEqualTo("First question");
    }

    @Test
    void startNewConversationClosesActiveConversation() {
        AiConversation existingConversation = conversation(42L, "First question", true);
        when(aiConversationRepository.findFirstByUserAndActiveTrueOrderByStartedAtDesc(user))
                .thenReturn(Optional.of(existingConversation));

        AiConversationSummaryDto summary = aiCoachService.startNewConversation(user);

        assertThat(summary.conversationId()).isEqualTo(42L);
        assertThat(summary.firstQuestion()).isEqualTo("First question");
        assertThat(existingConversation.isActive()).isFalse();
        assertThat(existingConversation.getEndedAt()).isNotNull();
    }

    @Test
    void recentConversationSummariesShowOnlyClosedConversationFirstQuestions() {
        AiConversation first = conversation(1L, "First question", false);
        AiConversation second = conversation(2L, "Another first question", false);
        when(aiConversationRepository.findByUserAndActiveFalseOrderByEndedAtDesc(user))
                .thenReturn(List.of(first, second));

        List<AiConversationSummaryDto> summaries = aiCoachService.getRecentConversationSummaries(user);

        assertThat(summaries).extracting(AiConversationSummaryDto::firstQuestion)
                .containsExactly("First question", "Another first question");
    }

    @Test
    void activeConversationMessagesAreLoadedOldestFirst() {
        AiConversation existingConversation = conversation(42L, "First question", true);
        AiMessage firstMessage = new AiMessage();
        AiMessage secondMessage = new AiMessage();

        when(aiConversationRepository.findFirstByUserAndActiveTrueOrderByStartedAtDesc(user))
                .thenReturn(Optional.of(existingConversation));
        when(aiMessageRepository.findByConversationOrderByCreatedAtAsc(existingConversation))
                .thenReturn(List.of(firstMessage, secondMessage));

        List<AiMessage> messages = aiCoachService.getActiveConversationMessages(user);

        assertThat(messages).containsExactly(firstMessage, secondMessage);
    }

    @Test
    void ownedConversationMessagesAreReturnedAsExchangeDtos() {
        AiConversation existingConversation = conversation(42L, "First question", false);
        AiMessage message = message(existingConversation, "First question", "Coach answer");
        when(aiConversationRepository.findByIdAndUser(42L, user)).thenReturn(Optional.of(existingConversation));
        when(aiMessageRepository.findByConversationOrderByCreatedAtAsc(existingConversation)).thenReturn(List.of(message));

        List<AiCoachExchangeResponseDto> messages = aiCoachService.getConversationMessages(42L, user);

        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).conversationId()).isEqualTo(42L);
        assertThat(messages.get(0).userMessage()).isEqualTo("First question");
        assertThat(messages.get(0).aiResponse()).isEqualTo("Coach answer");
    }

    @Test
    void missingConversationCannotBeRead() {
        when(aiConversationRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> aiCoachService.getConversationMessages(99L, user))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("AI conversation not found.");
    }

    private AiCoachResponseDto response(CoachIntent intent,
                                        RiskLevel riskLevel,
                                        ResponseType responseType,
                                        boolean directAnswerAllowed,
                                        String userFacingMessage,
                                        SuggestedAction suggestedAction) {
        AiCoachResponseDto response = new AiCoachResponseDto();
        response.setIntent(intent);
        response.setRiskLevel(riskLevel);
        response.setResponseType(responseType);
        response.setDirectAnswerAllowed(directAnswerAllowed);
        response.setUserFacingMessage(userFacingMessage);
        response.setSuggestedAction(suggestedAction);
        return response;
    }

    private AiConversation conversation(Long id, String firstQuestion, boolean active) {
        AiConversation conversation = new AiConversation();
        conversation.setId(id);
        conversation.setUser(user);
        conversation.setFirstQuestion(firstQuestion);
        conversation.setActive(active);
        return conversation;
    }

    private AiMessage message(AiConversation conversation, String userMessage, String aiResponse) {
        AiMessage message = new AiMessage();
        message.setConversation(conversation);
        message.setUser(user);
        message.setUserMessage(userMessage);
        message.setAiResponse(aiResponse);
        message.setIntent(CoachIntent.URGE_SUPPORT);
        message.setRiskLevel(RiskLevel.LOW);
        message.setResponseType(ResponseType.REFLECTION_PROMPT);
        return message;
    }
}
