package com.stark.steadyai.service;

import com.stark.steadyai.dto.AiCoachResponseDto;
import com.stark.steadyai.enums.CoachIntent;
import com.stark.steadyai.enums.ResponseType;
import com.stark.steadyai.enums.RiskLevel;
import com.stark.steadyai.enums.SuggestedAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class AiPolicyServiceTest {

    private AiPolicyService aiPolicyService;

    @BeforeEach
    void setUp() {
        aiPolicyService = new AiPolicyService();
    }

    @Test
    void directAnswerAllowedFalseReturnsBackendErpRedirect() {
        AiCoachResponseDto original = new AiCoachResponseDto();
        original.setIntent(CoachIntent.URGE_SUPPORT);
        original.setRiskLevel(RiskLevel.LOW);
        original.setResponseType(ResponseType.REFLECTION_PROMPT);
        original.setDirectAnswerAllowed(false);
        original.setUserFacingMessage("Some AI message");
        original.setSuggestedAction(SuggestedAction.START_DELAY_TIMER);

        AiCoachResponseDto result = aiPolicyService.applyPolicy(original);

        assertThat(result.getIntent()).isEqualTo(CoachIntent.REASSURANCE_SEEKING);
        assertThat(result.getResponseType()).isEqualTo(ResponseType.ERP_REDIRECT);
        assertThat(result.getUserFacingMessage()).contains("I cannot help you prove or disprove the fear");
    }

    @Test
    void crisisIntentReturnsStaticCrisisResponse() {
        AiCoachResponseDto original = new AiCoachResponseDto();
        original.setIntent(CoachIntent.CRISIS_OR_SELF_HARM);
        original.setRiskLevel(RiskLevel.HIGH);
        original.setResponseType(ResponseType.REFLECTION_PROMPT);
        original.setDirectAnswerAllowed(true);
        original.setUserFacingMessage("Some AI message");
        original.setSuggestedAction(SuggestedAction.NONE);

        AiCoachResponseDto result = aiPolicyService.applyPolicy(original);

        assertThat(result.getResponseType()).isEqualTo(ResponseType.CRISIS_STATIC_MESSAGE);
        assertThat(result.getUserFacingMessage()).contains("I am not equipped to help with immediate danger");
    }

    @Test
    void outOfScopeIntentReturnsStaticOutOfScopeResponse() {
        AiCoachResponseDto original = new AiCoachResponseDto();
        original.setIntent(CoachIntent.OUT_OF_SCOPE);
        original.setRiskLevel(RiskLevel.LOW);
        original.setResponseType(ResponseType.REFLECTION_PROMPT);
        original.setDirectAnswerAllowed(true);
        original.setUserFacingMessage("Some AI message");
        original.setSuggestedAction(SuggestedAction.NONE);

        AiCoachResponseDto result = aiPolicyService.applyPolicy(original);

        assertThat(result.getResponseType()).isEqualTo(ResponseType.OUT_OF_SCOPE_MESSAGE);
        assertThat(result.getUserFacingMessage()).contains("I can only help with urge logging");
    }
}
