package com.stark.steadyai.ai;

import com.stark.steadyai.dto.AiCoachRequestDto;
import com.stark.steadyai.dto.AiCoachResponseDto;
import com.stark.steadyai.enums.CoachIntent;
import com.stark.steadyai.enums.ResponseType;
import com.stark.steadyai.enums.RiskLevel;
import com.stark.steadyai.enums.SuggestedAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OpenAiCoachClientTest {

    private ChatClient.Builder chatClientBuilder;
    private ChatClient chatClient;
    private ChatClient.ChatClientRequestSpec requestSpec;
    private ChatClient.CallResponseSpec callResponseSpec;
    private AiResponseValidator validator;
    private OpenAiCoachClient openAiCoachClient;

    @BeforeEach
    @SuppressWarnings({"rawtypes", "unchecked"})
    void setUp() {
        chatClientBuilder = mock(ChatClient.Builder.class);
        chatClient = mock(ChatClient.class);
        requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        callResponseSpec = mock(ChatClient.CallResponseSpec.class);
        validator = mock(AiResponseValidator.class);

        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.advisors(any(Consumer.class))).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(any(Consumer.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);

        openAiCoachClient = new OpenAiCoachClient(chatClientBuilder, validator);
    }

    @Test
    void validStructuredResponseIsReturned() {
        AiCoachResponseDto validResponse = response(
                CoachIntent.URGE_SUPPORT,
                RiskLevel.LOW,
                ResponseType.REFLECTION_PROMPT,
                true,
                "Pause and notice the urge before acting.",
                SuggestedAction.START_DELAY_TIMER
        );

        when(callResponseSpec.entity(AiCoachResponseDto.class)).thenReturn(validResponse);
        when(validator.isInvalid(validResponse)).thenReturn(false);

        AiCoachResponseDto result = openAiCoachClient.generateResponse(new AiCoachRequestDto("I feel an urge"));

        assertThat(result).isSameAs(validResponse);
        verify(validator, never()).buildFallback();
    }

    @Test
    void nullParsedResponseReturnsValidatorFallback() {
        AiCoachResponseDto fallback = fallbackResponse();

        when(callResponseSpec.entity(AiCoachResponseDto.class)).thenReturn(null);
        when(validator.isInvalid(null)).thenReturn(true);
        when(validator.buildFallback()).thenReturn(fallback);

        AiCoachResponseDto result = openAiCoachClient.generateResponse(new AiCoachRequestDto("I feel anxious"));

        assertThat(result).isSameAs(fallback);
        verify(validator).buildFallback();
    }

    @Test
    void providerExceptionReturnsValidatorFallback() {
        AiCoachResponseDto fallback = fallbackResponse();

        when(chatClient.prompt()).thenThrow(new RuntimeException("provider unavailable"));
        when(validator.buildFallback()).thenReturn(fallback);

        AiCoachResponseDto result = openAiCoachClient.generateResponse(new AiCoachRequestDto("I feel anxious"));

        assertThat(result).isSameAs(fallback);
        verify(validator).buildFallback();
    }

    private AiCoachResponseDto fallbackResponse() {
        return response(
                CoachIntent.OUT_OF_SCOPE,
                RiskLevel.LOW,
                ResponseType.OUT_OF_SCOPE_MESSAGE,
                false,
                "I could not process that safely. Please try again with a clear message.",
                SuggestedAction.NONE
        );
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
}
