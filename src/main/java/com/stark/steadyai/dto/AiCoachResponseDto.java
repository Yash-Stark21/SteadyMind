package com.stark.steadyai.dto;

import com.stark.steadyai.enums.CoachIntent;
import com.stark.steadyai.enums.ResponseType;
import com.stark.steadyai.enums.RiskLevel;
import com.stark.steadyai.enums.SuggestedAction;

/**
 * Response DTO returned after the AI Coach pipeline processes a user message.
 * Contains the original message, AI response, classification metadata,
 * and Day 8 policy-layer fields (directAnswerAllowed, userFacingMessage, suggestedAction).
 */
public class AiCoachResponseDto {

    private String userMessage;
    private String aiResponse;
    private CoachIntent intent;
    private RiskLevel riskLevel;
    private ResponseType responseType;
    private boolean directAnswerAllowed;
    private String userFacingMessage;
    private SuggestedAction suggestedAction;

    public AiCoachResponseDto() {
    }

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public String getAiResponse() {
        return aiResponse;
    }

    public void setAiResponse(String aiResponse) {
        this.aiResponse = aiResponse;
    }

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
