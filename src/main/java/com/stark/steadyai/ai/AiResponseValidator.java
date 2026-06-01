package com.stark.steadyai.ai;

import com.stark.steadyai.dto.AiCoachResponseDto;
import com.stark.steadyai.enums.CoachIntent;
import com.stark.steadyai.enums.ResponseType;
import com.stark.steadyai.enums.RiskLevel;
import com.stark.steadyai.enums.SuggestedAction;
import com.stark.steadyai.exception.InvalidAiResponseException;
import org.springframework.stereotype.Component;

@Component
public class AiResponseValidator {

    public boolean isValid(AiCoachResponseDto response) {
        if (response == null) {
            return false;
        }

        if (response.getIntent() == null ||
                response.getRiskLevel() == null ||
                response.getResponseType() == null ||
                response.getSuggestedAction() == null) {
            return false;
        }

        if (response.getUserFacingMessage() == null ||
                response.getUserFacingMessage().isBlank() ||
                response.getUserFacingMessage().length() > 500) {
            return false;
        }

        return isValidBusinessCombination(response);
    }
    public boolean isInvalid(AiCoachResponseDto response) {
        return !isValid(response);
    }

    public void validate(AiCoachResponseDto response) {
        if (isInvalid(response)) {
            throw new InvalidAiResponseException(
                    "AI response failed structural or policy validation."
            );
        }
    }

    public AiCoachResponseDto buildFallback() {
        AiCoachResponseDto fallback = new AiCoachResponseDto();

        fallback.setIntent(CoachIntent.OUT_OF_SCOPE);
        fallback.setRiskLevel(RiskLevel.LOW);
        fallback.setResponseType(ResponseType.OUT_OF_SCOPE_MESSAGE);
        fallback.setDirectAnswerAllowed(false);
        fallback.setUserFacingMessage(
                "I could not process that safely. Please try again with a clear message."
        );
        fallback.setSuggestedAction(SuggestedAction.NONE);

        return fallback;
    }

    private boolean isValidBusinessCombination(AiCoachResponseDto response) {
        CoachIntent intent = response.getIntent();
        RiskLevel riskLevel = response.getRiskLevel();
        ResponseType responseType = response.getResponseType();
        SuggestedAction suggestedAction = response.getSuggestedAction();

        if (intent == CoachIntent.CRISIS_OR_SELF_HARM) {
            return riskLevel == RiskLevel.HIGH
                    && responseType == ResponseType.CRISIS_STATIC_MESSAGE
                    && suggestedAction == SuggestedAction.CONTACT_SUPPORT
                    && !response.isDirectAnswerAllowed();
        }

        if (intent == CoachIntent.REASSURANCE_SEEKING) {
            return responseType == ResponseType.ERP_REDIRECT
                    && suggestedAction == SuggestedAction.START_DELAY_TIMER
                    && !response.isDirectAnswerAllowed();
        }

        if (intent == CoachIntent.URGE_SUPPORT) {
            return responseType == ResponseType.REFLECTION_PROMPT
                    && suggestedAction == SuggestedAction.START_DELAY_TIMER;
        }

        if (intent == CoachIntent.EXPOSURE_REFLECTION) {
            return responseType == ResponseType.REFLECTION_PROMPT
                    && suggestedAction == SuggestedAction.VIEW_EXPOSURE_LIST;
        }

        if (intent == CoachIntent.GENERAL_EDUCATION) {
            return responseType == ResponseType.EDUCATIONAL
                    && suggestedAction == SuggestedAction.NONE;
        }

        if (intent == CoachIntent.OUT_OF_SCOPE) {
            return riskLevel == RiskLevel.LOW
                    && responseType == ResponseType.OUT_OF_SCOPE_MESSAGE
                    && suggestedAction == SuggestedAction.NONE
                    && !response.isDirectAnswerAllowed();
        }

        return false;
    }
}