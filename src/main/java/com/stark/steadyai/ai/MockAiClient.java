package com.stark.steadyai.ai;

import com.stark.steadyai.dto.AiCoachRequestDto;
import com.stark.steadyai.dto.AiCoachResponseDto;
import com.stark.steadyai.enums.CoachIntent;
import com.stark.steadyai.enums.ResponseType;
import com.stark.steadyai.enums.RiskLevel;
import com.stark.steadyai.enums.SuggestedAction;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Mock AI client that returns deterministic, keyword-based responses.
 * Active only when the "mock-ai" Spring profile is enabled.
 *
 * This allows the full AI Coach pipeline to run without an OpenAI API key
 * or any external network dependency.
 */
@Component
@Profile("mock-ai")
public class MockAiClient implements AiClient {

    @Override
    public AiCoachResponseDto generateResponse(AiCoachRequestDto requestDto) {
        String message = requestDto.getMessage().trim().toLowerCase();

        // --- Crisis / self-harm ---
        if (containsAny(message, "suicide", "kill myself", "end my life",
                "self harm", "self-harm", "hurt myself", "harm myself")) {
            return buildResponse(
                    CoachIntent.CRISIS_OR_SELF_HARM,
                    RiskLevel.HIGH,
                    ResponseType.CRISIS_STATIC_MESSAGE,
                    false,
                    "I am not equipped to help with immediate danger. "
                            + "If you may hurt yourself or someone else, contact local "
                            + "emergency services now or reach a trusted person nearby.",
                    SuggestedAction.CONTACT_SUPPORT
            );
        }

        // --- Reassurance-seeking ---
        if (containsAny(message, "promise", "guarantee", "confirm",
                "sure nothing", "am i safe", "is it contaminated",
                "should i check")) {
            return buildResponse(
                    CoachIntent.REASSURANCE_SEEKING,
                    RiskLevel.LOW,
                    ResponseType.ERP_REDIRECT,
                    false,
                    "I cannot help you prove or disprove the fear. "
                            + "Let us practice sitting with uncertainty. "
                            + "Choose a short delay and notice the urge without obeying it.",
                    SuggestedAction.START_DELAY_TIMER
            );
        }

        // --- Urge / compulsion support ---
        if (containsAny(message, "urge", "craving", "compulsion",
                "ritual", "checking")) {
            return buildResponse(
                    CoachIntent.URGE_SUPPORT,
                    RiskLevel.LOW,
                    ResponseType.REFLECTION_PROMPT,
                    true,
                    "Pause for one minute. Notice the urge without solving it. "
                            + "Rate the intensity, breathe slowly, and choose a short "
                            + "delay before acting.",
                    SuggestedAction.START_DELAY_TIMER
            );
        }

        // --- Exposure / avoidance reflection ---
        if (containsAny(message, "exposure", "fear", "avoid", "task")) {
            return buildResponse(
                    CoachIntent.EXPOSURE_REFLECTION,
                    RiskLevel.LOW,
                    ResponseType.REFLECTION_PROMPT,
                    true,
                    "Choose the smallest exposure step you can repeat safely. "
                            + "The goal is practice, not certainty.",
                    SuggestedAction.VIEW_EXPOSURE_LIST
            );
        }

        // --- Default: general education ---
        return buildResponse(
                CoachIntent.GENERAL_EDUCATION,
                RiskLevel.LOW,
                ResponseType.EDUCATIONAL,
                true,
                "Let us slow this down. Describe the trigger, the urge, "
                        + "and the next small action you can take.",
                SuggestedAction.NONE
        );
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private AiCoachResponseDto buildResponse(CoachIntent intent, RiskLevel riskLevel,
                                              ResponseType responseType, boolean directAnswerAllowed,
                                              String userFacingMessage, SuggestedAction suggestedAction) {
        AiCoachResponseDto dto = new AiCoachResponseDto();
        dto.setIntent(intent);
        dto.setRiskLevel(riskLevel);
        dto.setResponseType(responseType);
        dto.setDirectAnswerAllowed(directAnswerAllowed);
        dto.setUserFacingMessage(userFacingMessage);
        dto.setSuggestedAction(suggestedAction);
        return dto;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
