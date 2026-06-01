package com.stark.steadyai.ai;

import com.stark.steadyai.dto.AiCoachRequestDto;
import com.stark.steadyai.dto.AiCoachResponseDto;
import com.stark.steadyai.enums.CoachIntent;
import com.stark.steadyai.enums.ResponseType;
import com.stark.steadyai.enums.RiskLevel;
import com.stark.steadyai.enums.SuggestedAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.AdvisorParams;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("openai")
public class SpringAiClient implements AiClient {

    private static final Logger log = LoggerFactory.getLogger(SpringAiClient.class);

    private final ChatClient chatClient;
    private final AiResponseValidator validator;

    public SpringAiClient(ChatClient.Builder chatClientBuilder,
                          AiResponseValidator validator) {
        this.chatClient = chatClientBuilder.build();
        this.validator = validator;
    }

    @Override
    public AiCoachResponseDto generateResponse(AiCoachRequestDto requestDto) {
        try {
            AiCoachResponseDto response = chatClient.prompt()
                    .advisors(AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT)
                    .system(buildSystemPrompt())
                    .user(userSpec -> userSpec
                            .text("""
                                    Classify the following user message according to the allowed response contract.

                                    User message:
                                    {message}
                                    """)
                            .param("message", safeMessage(requestDto)))
                    .call()
                    .entity(AiCoachResponseDto.class);

            if (validator.isInvalid(response)) {
                log.warn("AI response failed validation. Returning safe fallback. Response: {}", response);
                return safeFallbackResponse();
            }

            return response;

        } catch (Exception e) {
            log.error("OpenAI/Spring AI response generation failed. Returning safe fallback.", e);
            return safeFallbackResponse();
        }
    }

    private String safeMessage(AiCoachRequestDto requestDto) {
        if (requestDto == null || requestDto.message() == null || requestDto.message().isBlank()) {
            return "Empty message";
        }

        return requestDto.message().trim();
    }

    private AiCoachResponseDto safeFallbackResponse() {
        AiCoachResponseDto dto = new AiCoachResponseDto();
        dto.setIntent(CoachIntent.OUT_OF_SCOPE);
        dto.setRiskLevel(RiskLevel.LOW);
        dto.setResponseType(ResponseType.OUT_OF_SCOPE_MESSAGE);
        dto.setDirectAnswerAllowed(false);
        dto.setUserFacingMessage(
                "I could not process that safely. Please try again with a clear message."
        );
        dto.setSuggestedAction(SuggestedAction.NONE);
        return dto;
    }

    private String buildSystemPrompt() {
        return """
                You are not a therapist, doctor, or emergency service.
                You are a structured classification assistant inside a Spring Boot backend.

                Your job is to classify the user's message and produce an AiCoachResponseDto object.

                Do not diagnose.
                Do not promise safety.
                Do not give direct reassurance.
                Do not encourage checking, compulsions, reassurance seeking, confession, rumination, or repeated review.

                Allowed intent values:
                - URGE_SUPPORT
                - REASSURANCE_SEEKING
                - EXPOSURE_REFLECTION
                - GENERAL_EDUCATION
                - CRISIS_OR_SELF_HARM
                - OUT_OF_SCOPE

                Allowed riskLevel values:
                - LOW
                - MEDIUM
                - HIGH

                Allowed responseType values:
                - ERP_REDIRECT
                - REFLECTION_PROMPT
                - EDUCATIONAL
                - CRISIS_STATIC_MESSAGE
                - OUT_OF_SCOPE_MESSAGE

                Allowed suggestedAction values:
                - START_DELAY_TIMER
                - LOG_URGE
                - VIEW_EXPOSURE_LIST
                - CONTACT_SUPPORT
                - NONE

                Classification rules:

                If the user asks for certainty, promise, confirmation, contamination reassurance,
                harm reassurance, or asks whether to check:
                - intent = REASSURANCE_SEEKING
                - responseType = ERP_REDIRECT
                - directAnswerAllowed = false
                - suggestedAction = START_DELAY_TIMER

                If the user expresses self-harm, suicide, intent to die, or immediate danger:
                - intent = CRISIS_OR_SELF_HARM
                - riskLevel = HIGH
                - responseType = CRISIS_STATIC_MESSAGE
                - directAnswerAllowed = false
                - suggestedAction = CONTACT_SUPPORT

                If the user asks about an urge, craving, ritual, checking, or compulsion:
                - intent = URGE_SUPPORT
                - responseType = REFLECTION_PROMPT
                - suggestedAction = START_DELAY_TIMER

                If the user discusses an exposure task, fear practice, avoidance, or exposure reflection:
                - intent = EXPOSURE_REFLECTION
                - responseType = REFLECTION_PROMPT
                - suggestedAction = VIEW_EXPOSURE_LIST

                If the user asks for general learning or non-diagnostic education:
                - intent = GENERAL_EDUCATION
                - responseType = EDUCATIONAL
                - suggestedAction = NONE

                If the message is unrelated to the app:
                - intent = OUT_OF_SCOPE
                - responseType = OUT_OF_SCOPE_MESSAGE
                - directAnswerAllowed = false
                - suggestedAction = NONE

                The userFacingMessage must be short, practical, non-diagnostic, and safe.
                """;
    }
}