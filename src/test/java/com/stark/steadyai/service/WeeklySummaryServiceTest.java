package com.stark.steadyai.service;

import com.stark.steadyai.ai.AiClient;
import com.stark.steadyai.dto.AiCoachRequestDto;
import com.stark.steadyai.dto.AiCoachResponseDto;
import com.stark.steadyai.dto.WeeklySummaryResponse;
import com.stark.steadyai.entity.UrgeLog;
import com.stark.steadyai.entity.User;
import com.stark.steadyai.repository.UrgeLogRepository;
import com.stark.steadyai.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.AfterEach;
import com.stark.steadyai.security.SecurityUtils;
import static org.mockito.Mockito.mockStatic;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeeklySummaryServiceTest {

    @Mock
    private UrgeLogRepository urgeLogRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AiClient aiClient;

    private WeeklySummaryService weeklySummaryService;
    private User testUser;
    private MockedStatic<SecurityUtils> mockedSecurityUtils;

    @BeforeEach
    void setUp() {
        weeklySummaryService = new WeeklySummaryService(urgeLogRepository, userRepository, aiClient);
        testUser = new User("Demo User", "demo@steadyai.local", "hash");
        lenient().when(userRepository.findByEmail("demo@steadyai.local")).thenReturn(Optional.of(testUser));
        mockedSecurityUtils = mockStatic(SecurityUtils.class);
        mockedSecurityUtils.when(SecurityUtils::getCurrentUser).thenReturn(testUser);
    }

    @AfterEach
    void tearDown() {
        if (mockedSecurityUtils != null) {
            mockedSecurityUtils.close();
        }
    }

    @Test
    void getWeeklySummary_EmptyLogs() {
        when(urgeLogRepository.findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(eq(testUser), any(), any()))
                .thenReturn(Collections.emptyList());

        WeeklySummaryResponse response = weeklySummaryService.getWeeklySummary();

        assertEquals(0, response.getTotalUrgeLogs());
        assertEquals("N/A", response.getMostCommonTrigger());
        verify(aiClient, never()).generateResponse(any());
    }

    @Test
    void getWeeklySummary_WithLogs() {
        UrgeLog log1 = new UrgeLog();
        log1.setIntensityBefore(8);
        log1.setTriggerText("Stress");
        setCreatedAt(log1, LocalDateTime.now().withHour(18)); // Evening

        UrgeLog log2 = new UrgeLog();
        log2.setIntensityBefore(6);
        log2.setTriggerText("Stress");
        setCreatedAt(log2, LocalDateTime.now().withHour(19)); // Evening

        UrgeLog log3 = new UrgeLog();
        log3.setIntensityBefore(4);
        log3.setTriggerText("Boredom");
        setCreatedAt(log3, LocalDateTime.now().withHour(10)); // Morning

        when(urgeLogRepository.findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(eq(testUser), any(), any()))
                .thenReturn(Arrays.asList(log1, log2, log3));

        AiCoachResponseDto mockResponse = new AiCoachResponseDto();
        mockResponse.setUserFacingMessage("Progress: Good. | Patterns: Evening stress. | Steps: Delay.");
        when(aiClient.generateResponse(any())).thenReturn(mockResponse);

        WeeklySummaryResponse response = weeklySummaryService.getWeeklySummary();

        assertEquals(3, response.getTotalUrgeLogs());
        assertEquals(6.0, response.getAverageIntensity());
        assertEquals("Stress", response.getMostCommonTrigger());
        assertEquals("Evening", response.getHighestRiskPeriod());
        assertEquals("Good.", response.getProgressObservations());
        assertEquals("Evening stress.", response.getRecurringPatterns());
        assertEquals("Delay.", response.getSuggestedNextSteps());
    }

    @Test
    void getWeeklySummary_SafeFallback() {
        UrgeLog log = new UrgeLog();
        log.setIntensityBefore(5);
        log.setTriggerText("Random");
        setCreatedAt(log, LocalDateTime.now().withHour(12));

        when(urgeLogRepository.findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(eq(testUser), any(), any()))
                .thenReturn(Collections.singletonList(log));

        AiCoachResponseDto mockResponse = new AiCoachResponseDto();
        // Return a response that triggers the safety override
        mockResponse.setUserFacingMessage("Progress: Bad. | Patterns: You are diagnosed with OCD | Steps: Get cured.");
        when(aiClient.generateResponse(any())).thenReturn(mockResponse);

        WeeklySummaryResponse response = weeklySummaryService.getWeeklySummary();

        // Should use fallback text instead of the unsafe text
        assertEquals("Keep practicing delays and noting your triggers.", response.getProgressObservations());
        assertEquals("Focus on small steps.", response.getRecurringPatterns());
        assertEquals("Discuss your logs with your care provider.", response.getSuggestedNextSteps());
    }

    private void setCreatedAt(UrgeLog log, LocalDateTime time) {
        try {
            var field = UrgeLog.class.getDeclaredField("createdAt");
            field.setAccessible(true);
            field.set(log, time);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
