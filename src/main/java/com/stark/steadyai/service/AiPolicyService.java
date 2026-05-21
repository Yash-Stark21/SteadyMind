package com.stark.steadyai.service;

import com.stark.steadyai.dto.AiCoachResponseDto;
import com.stark.steadyai.enums.CoachIntent;
import com.stark.steadyai.enums.ResponseType;
import com.stark.steadyai.enums.RiskLevel;
import com.stark.steadyai.enums.SuggestedAction;
import org.springframework.stereotype.Service;

/**
 * Final backend-controlled policy layer.
 *
 * Runs AFTER the AI client returns a response and enforces safety rules
 * that the backend — not the AI model — controls.
 *
 * This service proves the backend does not blindly trust AI output.
 */
@Service
public class AiPolicyService {

    private static final String SAFE_FALLBACK_MESSAGE =
            "I cannot give a direct reassurance answer. Let us return to the "
                    + "next helpful action: pause, label the urge, and choose a short delay.";

    /**
     * Applies backend policy rules to an AI-generated response.
     *
     * Rules enforced:
     * 1. Null response → safe fallback.
     * 2. HIGH risk → crisis static message override.
     * 3. directAnswerAllowed == false with ERP_REDIRECT → predefined ERP redirect.
     * 4. Missing responseType → safe fallback.
     * 5. Blank userFacingMessage → safe fallback.
     *
     * @param aiResponse the response from the AiClient (may be null)
     * @return the policy-enforced, safe response
     */
    public AiCoachResponseDto applyPolicy(AiCoachResponseDto aiResponse) {

        // Rule 1: null response
        if (aiResponse == null) {
            return buildSafeFallback();
        }

        // Rule 2: Crisis intent override
        if (aiResponse.getIntent() == CoachIntent.CRISIS_OR_SELF_HARM) {
            return buildCrisisOverride(aiResponse);
        }

        // Rule 3: Out of scope intent override
        if (aiResponse.getIntent() == CoachIntent.OUT_OF_SCOPE) {
            return buildOutOfScopeOverride(aiResponse);
        }

        // Rule 4: ERP redirect when direct answer is not allowed
        if (!aiResponse.isDirectAnswerAllowed()) {
            return buildErpRedirectOverride(aiResponse);
        }

        // Rule 5: missing response type
        if (aiResponse.getResponseType() == null) {
            return buildSafeFallback();
        }

        // Rule 6: blank user-facing message
        if (aiResponse.getUserFacingMessage() == null
                || aiResponse.getUserFacingMessage().isBlank()) {
            return buildSafeFallback();
        }

        // All rules passed — response is safe to return as-is
        return aiResponse;
    }

    // =========================================================================
    // Override Builders
    // =========================================================================

    private AiCoachResponseDto buildCrisisOverride(AiCoachResponseDto original) {
        original.setIntent(CoachIntent.CRISIS_OR_SELF_HARM);
        original.setRiskLevel(RiskLevel.HIGH);
        original.setResponseType(ResponseType.CRISIS_STATIC_MESSAGE);
        original.setDirectAnswerAllowed(false);
        original.setUserFacingMessage(
                "I am not equipped to help with immediate danger. "
                        + "If you may hurt yourself or someone else, contact local "
                        + "emergency services now or reach a trusted person nearby.");
        original.setSuggestedAction(SuggestedAction.CONTACT_SUPPORT);
        return original;
    }

    private AiCoachResponseDto buildOutOfScopeOverride(AiCoachResponseDto original) {
        original.setIntent(CoachIntent.OUT_OF_SCOPE);
        original.setRiskLevel(RiskLevel.LOW);
        original.setResponseType(ResponseType.OUT_OF_SCOPE_MESSAGE);
        original.setDirectAnswerAllowed(false);
        original.setUserFacingMessage("I can only help with urge logging, exposure reflection, progress summaries, and non-diagnostic coping support inside SteadyAI.");
        original.setSuggestedAction(SuggestedAction.NONE);
        return original;
    }

    private AiCoachResponseDto buildErpRedirectOverride(AiCoachResponseDto original) {
        original.setIntent(CoachIntent.REASSURANCE_SEEKING);
        original.setRiskLevel(RiskLevel.LOW);
        original.setResponseType(ResponseType.ERP_REDIRECT);
        original.setDirectAnswerAllowed(false);
        original.setUserFacingMessage(
                "I cannot help you prove or disprove the fear. "
                        + "Let us practice sitting with uncertainty. "
                        + "Choose a short delay and notice the urge without obeying it.");
        original.setSuggestedAction(SuggestedAction.START_DELAY_TIMER);
        return original;
    }

    private AiCoachResponseDto buildSafeFallback() {
        AiCoachResponseDto dto = new AiCoachResponseDto();
        dto.setIntent(CoachIntent.OUT_OF_SCOPE);
        dto.setRiskLevel(RiskLevel.LOW);
        dto.setResponseType(ResponseType.OUT_OF_SCOPE_MESSAGE);
        dto.setDirectAnswerAllowed(false);
        dto.setUserFacingMessage(SAFE_FALLBACK_MESSAGE);
        dto.setSuggestedAction(SuggestedAction.NONE);
        return dto;
    }
}
