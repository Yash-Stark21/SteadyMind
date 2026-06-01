package com.stark.steadyai.service;

import com.stark.steadyai.ai.AiClient;
import com.stark.steadyai.ai.AiResponseValidator;
import com.stark.steadyai.dto.AiCoachRequestDto;
import com.stark.steadyai.dto.AiCoachResponseDto;
import com.stark.steadyai.entity.AiMessage;
import com.stark.steadyai.entity.User;
import com.stark.steadyai.enums.CoachIntent;
import com.stark.steadyai.enums.ResponseType;
import com.stark.steadyai.enums.RiskLevel;
import com.stark.steadyai.enums.SuggestedAction;
import com.stark.steadyai.exception.InvalidAiResponseException;
import com.stark.steadyai.repository.AiMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AiCoachServiceTest {

    @Mock
    private AiMessageRepository aiMessageRepository;

    @Mock
    private AiClient aiClient;

    @Mock
    private AiSafetyService aiSafetyService;

    @Mock
    private AiPolicyService aiPolicyService;

    @Mock
    private AiResponseValidator aiResponseValidator;

    @InjectMocks
    private AiCoachServiceImpl aiCoachService;

    private User user;
    private AiCoachRequestDto requestDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1L);
    }

    @Test
    void aiClientFailureReturnsSafeFallback() {
        requestDto = new AiCoachRequestDto("I am anxious");
        when(aiSafetyService.isCrisisOrSelfHarm(any())).thenReturn(false);
        when(aiSafetyService.isReassuranceSeeking(any())).thenReturn(false);
        when(aiClient.generateResponse(any())).thenThrow(new RuntimeException("API down"));
        
        AiCoachResponseDto fallbackResponse = new AiCoachResponseDto();
        fallbackResponse.setResponseType(ResponseType.ERP_REDIRECT);
        fallbackResponse.setUserFacingMessage("I cannot verify or disprove the fear for you");
        when(aiResponseValidator.buildFallback()).thenReturn(fallbackResponse);
        
        // Mock policy service to just return what it gets (fallback)
        when(aiPolicyService.applyPolicy(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AiCoachResponseDto result = aiCoachService.processMessage(requestDto, user);

        assertThat(result.getResponseType()).isEqualTo(ResponseType.ERP_REDIRECT);
        assertThat(result.getUserFacingMessage()).contains("I cannot verify or disprove the fear for you");
        verify(aiMessageRepository).save(any(AiMessage.class));
    }

    @Test
    void invalidAiResponseReturnsSafeFallback() {
        requestDto = new AiCoachRequestDto("I am anxious");
        when(aiSafetyService.isCrisisOrSelfHarm(any())).thenReturn(false);
        when(aiSafetyService.isReassuranceSeeking(any())).thenReturn(false);
        AiCoachResponseDto invalidResponse = new AiCoachResponseDto();
        when(aiClient.generateResponse(any())).thenReturn(invalidResponse);
        doThrow(new InvalidAiResponseException("Invalid")).when(aiResponseValidator).validate(invalidResponse);
        
        AiCoachResponseDto fallbackResponse = new AiCoachResponseDto();
        fallbackResponse.setResponseType(ResponseType.ERP_REDIRECT);
        fallbackResponse.setUserFacingMessage("I cannot verify or disprove the fear for you");
        when(aiResponseValidator.buildFallback()).thenReturn(fallbackResponse);
        
        when(aiPolicyService.applyPolicy(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AiCoachResponseDto result = aiCoachService.processMessage(requestDto, user);

        assertThat(result.getResponseType()).isEqualTo(ResponseType.ERP_REDIRECT);
        assertThat(result.getUserFacingMessage()).contains("I cannot verify or disprove the fear for you");
        verify(aiMessageRepository).save(any(AiMessage.class));
    }

    @Test
    void reassuranceSeekingPreCheckBypassesAiClient() {
        requestDto = new AiCoachRequestDto("Can you promise?");
        when(aiSafetyService.isCrisisOrSelfHarm(any())).thenReturn(false);
        when(aiSafetyService.isReassuranceSeeking(any())).thenReturn(true);
        
        AiCoachResponseDto redirectDto = new AiCoachResponseDto();
        redirectDto.setIntent(CoachIntent.REASSURANCE_SEEKING);
        when(aiSafetyService.buildReassuranceRedirectResponse()).thenReturn(redirectDto);

        aiCoachService.processMessage(requestDto, user);

        verify(aiClient, never()).generateResponse(any());
        verify(aiMessageRepository).save(any(AiMessage.class));
    }

    @Test
    void crisisPreCheckBypassesAiClient() {
        requestDto = new AiCoachRequestDto("I want to kill myself");
        when(aiSafetyService.isCrisisOrSelfHarm(any())).thenReturn(true);
        
        AiCoachResponseDto crisisDto = new AiCoachResponseDto();
        crisisDto.setIntent(CoachIntent.CRISIS_OR_SELF_HARM);
        when(aiSafetyService.buildCrisisResponse()).thenReturn(crisisDto);

        aiCoachService.processMessage(requestDto, user);

        verify(aiClient, never()).generateResponse(any());
        ArgumentCaptor<AiMessage> captor = ArgumentCaptor.forClass(AiMessage.class);
        verify(aiMessageRepository).save(captor.capture());
        assertThat(captor.getValue().getUserMessage()).isEqualTo("[MINIMAL METADATA]");
    }

    @Test
    void validAiResponseIsSavedToRepository() {
        requestDto = new AiCoachRequestDto("Normal message");
        when(aiSafetyService.isCrisisOrSelfHarm(any())).thenReturn(false);
        when(aiSafetyService.isReassuranceSeeking(any())).thenReturn(false);
        
        AiCoachResponseDto validResponse = new AiCoachResponseDto();
        validResponse.setIntent(CoachIntent.URGE_SUPPORT);
        validResponse.setRiskLevel(RiskLevel.LOW);
        validResponse.setResponseType(ResponseType.REFLECTION_PROMPT);
        validResponse.setUserFacingMessage("Great job");
        validResponse.setSuggestedAction(SuggestedAction.NONE);
        
        when(aiClient.generateResponse(any())).thenReturn(validResponse);
        when(aiPolicyService.applyPolicy(any())).thenReturn(validResponse);

        aiCoachService.processMessage(requestDto, user);

        ArgumentCaptor<AiMessage> captor = ArgumentCaptor.forClass(AiMessage.class);
        verify(aiMessageRepository).save(captor.capture());
        assertThat(captor.getValue().getAiResponse()).isEqualTo("Great job");
    }
}
