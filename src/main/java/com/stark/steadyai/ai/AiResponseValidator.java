package com.stark.steadyai.ai;

import com.stark.steadyai.dto.AiCoachResponseDto;
import com.stark.steadyai.enums.CoachIntent;
import com.stark.steadyai.enums.ResponseType;
import com.stark.steadyai.enums.RiskLevel;
import com.stark.steadyai.enums.SuggestedAction;
import org.springframework.stereotype.Component;

@Component
public class AiResponseValidator {

    public void validate(AiCoachResponseDto response) {
        if (response == null ||
            response.getIntent() == null ||
            response.getRiskLevel() == null ||
            response.getResponseType() == null ||
            response.getSuggestedAction() == null ||
            response.getUserFacingMessage() == null ||
            response.getUserFacingMessage().isBlank()) {
            
            throw new com.stark.steadyai.exception.InvalidAiResponseException("AI Response is missing required fields or has blank content.");
        }
    }
    public AiCoachResponseDto buildFallback() {
        AiCoachResponseDto fallback = new AiCoachResponseDto();
        fallback.setIntent(CoachIntent.OUT_OF_SCOPE);
        fallback.setRiskLevel(RiskLevel.LOW);
        fallback.setResponseType(ResponseType.OUT_OF_SCOPE_MESSAGE);
        fallback.setDirectAnswerAllowed(false);
        fallback.setUserFacingMessage("I cannot give a direct reassurance answer. Let us return to a safe next step: pause, label the urge, and choose a short delay.");
        fallback.setSuggestedAction(SuggestedAction.NONE);
        return fallback;
    }
}
