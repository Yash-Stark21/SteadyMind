package com.stark.steadyai.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class AiSafetyServiceTest {

    private AiSafetyService aiSafetyService;

    @BeforeEach
    void setUp() {
        aiSafetyService = new AiSafetyService();
    }

    @Test
    void reassuranceSeekingMessageIsDetected() {
        assertThat(aiSafetyService.isReassuranceSeeking("Can you promise me?")).isTrue();
        assertThat(aiSafetyService.isReassuranceSeeking("Am I contaminated?")).isTrue();
    }

    @Test
    void normalUrgeSupportMessageIsNotClassifiedAsReassurance() {
        assertThat(aiSafetyService.isReassuranceSeeking("I am feeling an urge right now")).isFalse();
    }

    @Test
    void crisisMessageIsDetected() {
        assertThat(aiSafetyService.isCrisisOrSelfHarm("I want to kill myself")).isTrue();
    }

    @Test
    void nullMessageDoesNotCrash() {
        assertThat(aiSafetyService.isReassuranceSeeking(null)).isFalse();
        assertThat(aiSafetyService.isCrisisOrSelfHarm(null)).isFalse();
    }

    @Test
    void blankMessageDoesNotCrash() {
        assertThat(aiSafetyService.isReassuranceSeeking("   ")).isFalse();
        assertThat(aiSafetyService.isCrisisOrSelfHarm("   ")).isFalse();
    }
}
