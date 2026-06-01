package com.stark.steadyai.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.stark.steadyai.enums.CoachIntent;
import com.stark.steadyai.enums.ResponseType;
import com.stark.steadyai.enums.RiskLevel;
import com.stark.steadyai.enums.SuggestedAction;

@JsonPropertyOrder({
        "intent",
        "riskLevel",
        "responseType",
        "directAnswerAllowed",
        "userFacingMessage",
        "suggestedAction"
})
public class AiCoachResponseDto {

    private CoachIntent intent;
    private RiskLevel riskLevel;
    private ResponseType responseType;
    private boolean directAnswerAllowed;
    private String userFacingMessage;
    private SuggestedAction suggestedAction;

    public CoachIntent getIntent() {
        return intent;
    }

    public void setIntent(CoachIntent intent) {
        this.intent = intent;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public ResponseType getResponseType() {
        return responseType;
    }

    public void setResponseType(ResponseType responseType) {
        this.responseType = responseType;
    }

    public boolean isDirectAnswerAllowed() {
        return directAnswerAllowed;
    }

    public void setDirectAnswerAllowed(boolean directAnswerAllowed) {
        this.directAnswerAllowed = directAnswerAllowed;
    }

    public String getUserFacingMessage() {
        return userFacingMessage;
    }

    public void setUserFacingMessage(String userFacingMessage) {
        this.userFacingMessage = userFacingMessage;
    }

    public SuggestedAction getSuggestedAction() {
        return suggestedAction;
    }

    public void setSuggestedAction(SuggestedAction suggestedAction) {
        this.suggestedAction = suggestedAction;
    }
}