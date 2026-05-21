package com.stark.steadyai.service;

import com.stark.steadyai.dto.AiCoachResponseDto;
import com.stark.steadyai.enums.CoachIntent;
import com.stark.steadyai.enums.ResponseType;
import com.stark.steadyai.enums.RiskLevel;
import com.stark.steadyai.enums.SuggestedAction;
import org.springframework.stereotype.Service;

/**
 * Safety pre-check layer that runs BEFORE any AI client is invoked.
 *
 * Responsibilities:
 * - Detect crisis / self-harm language and return a static safe response.
 * - Detect reassurance-seeking patterns and redirect to ERP.
 *
 * If either check triggers, the AiClient is bypassed entirely.
 */
@Service
public class AiSafetyService {

    // =========================================================================
    // Crisis Detection
    // =========================================================================

    /**
     * Returns true if the message contains self-harm or suicide language.
     */
    public boolean isCrisisMessage(String message) {
        String lower = message.toLowerCase();
        return containsAny(lower,
                "suicide", "kill myself", "end my life",
                "self harm", "self-harm", "hurt myself", "harm myself");
    }

    /**
     * Builds a static, clinically-safe crisis response.
     * No AI model output is included.
     */
    public AiCoachResponseDto buildCrisisResponse() {
        AiCoachResponseDto dto = new AiCoachResponseDto();
        dto.setIntent(CoachIntent.CRISIS_OR_SELF_HARM);
        dto.setRiskLevel(RiskLevel.HIGH);
        dto.setResponseType(ResponseType.CRISIS_STATIC_MESSAGE);
        dto.setDirectAnswerAllowed(false);
        dto.setUserFacingMessage(
                "I am not equipped to help with immediate danger. "
                        + "If you may hurt yourself or someone else, contact local "
                        + "emergency services now or reach a trusted person nearby.");
        dto.setSuggestedAction(SuggestedAction.CONTACT_SUPPORT);
        return dto;
    }

    // =========================================================================
    // Reassurance-Seeking Detection
    // =========================================================================

    /**
     * Returns true if the message appears to be seeking reassurance.
     * Reassurance-seeking is a common OCD compulsion and must be redirected,
     * not answered directly.
     */
    public boolean isReassuranceSeeking(String message) {
        String lower = message.toLowerCase();
        return containsAny(lower,
                "promise", "guarantee", "confirm",
                "sure nothing", "am i safe", "is it contaminated",
                "should i check", "can you tell me for sure",
                "am i contaminated", "did i harm someone");
    }

    /**
     * Builds a predefined ERP redirect response.
     * Avoids providing the reassurance the user is seeking.
     */
    public AiCoachResponseDto buildReassuranceRedirectResponse() {
        AiCoachResponseDto dto = new AiCoachResponseDto();
        dto.setIntent(CoachIntent.REASSURANCE_SEEKING);
        dto.setRiskLevel(RiskLevel.LOW);
        dto.setResponseType(ResponseType.ERP_REDIRECT);
        dto.setDirectAnswerAllowed(false);
        dto.setUserFacingMessage(
                "I cannot help you prove or disprove the fear. "
                        + "Let us practice sitting with uncertainty. "
                        + "Choose a short delay and notice the urge without obeying it.");
        dto.setSuggestedAction(SuggestedAction.START_DELAY_TIMER);
        return dto;
    }

    // =========================================================================
    // Utility
    // =========================================================================

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
