package com.stark.steadyai.service;

import com.stark.steadyai.dto.IntensityDistributionResponse;
import com.stark.steadyai.dto.ProgressAnalyticsResponse;
import com.stark.steadyai.dto.TrendPointResponse;
import com.stark.steadyai.dto.TriggerBreakdownResponse;
import com.stark.steadyai.entity.CompulsionDelayAttempt;
import com.stark.steadyai.entity.ExposureTask;
import com.stark.steadyai.entity.UrgeLog;
import com.stark.steadyai.entity.User;
import com.stark.steadyai.enums.CompulsionDelayOutcome;
import com.stark.steadyai.enums.ExposureDifficulty;
import com.stark.steadyai.enums.ExposureStatus;
import com.stark.steadyai.repository.CompulsionDelayAttemptRepository;
import com.stark.steadyai.repository.ExposureTaskRepository;
import com.stark.steadyai.repository.UrgeLogRepository;
import com.stark.steadyai.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.AfterEach;
import com.stark.steadyai.security.SecurityUtils;
import static org.mockito.Mockito.mockStatic;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProgressAnalyticsServiceTest {

    @Mock
    private UrgeLogRepository urgeLogRepository;

    @Mock
    private ExposureTaskRepository exposureTaskRepository;

    @Mock
    private CompulsionDelayAttemptRepository compulsionDelayAttemptRepository;

    @Mock
    private UserRepository userRepository;

    private ProgressAnalyticsService service;
    private User testUser;
    private MockedStatic<SecurityUtils> mockedSecurityUtils;

    @BeforeEach
    void setUp() {
        service = new ProgressAnalyticsService(
                urgeLogRepository, exposureTaskRepository,
                compulsionDelayAttemptRepository, userRepository);

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
    void getProgressAnalytics_NoData_ReturnsSafeEmptyResponse() {
        when(urgeLogRepository.findByUser(testUser)).thenReturn(Collections.emptyList());
        when(exposureTaskRepository.findByUserOrderByDifficultyLevelAsc(testUser)).thenReturn(Collections.emptyList());
        when(compulsionDelayAttemptRepository.findByUser(testUser)).thenReturn(Collections.emptyList());
        when(urgeLogRepository.findByUserAndCreatedAtBetweenOrderByCreatedAtAsc(eq(testUser), any(), any()))
                .thenReturn(Collections.emptyList());

        ProgressAnalyticsResponse response = service.getProgressAnalytics();

        assertEquals(0, response.totalUrgeLogs());
        assertEquals(0.0, response.averageUrgeIntensity());
        assertEquals(0, response.highestUrgeIntensity());
        assertEquals(0, response.totalExposureTasks());
        assertEquals(0, response.completedExposureTasks());
        assertEquals(0, response.pendingExposureTasks());
        assertEquals(0, response.totalDelayAttempts());
        assertEquals(0, response.completedDelayAttempts());
        assertEquals(0, response.cancelledDelayAttempts());
        assertEquals(0.0, response.averageDelayMinutes());
        assertNotNull(response.sevenDayUrgeTrend());
        assertEquals(7, response.sevenDayUrgeTrend().size());
        assertNotNull(response.triggerBreakdown());
        assertTrue(response.triggerBreakdown().isEmpty());
        assertNotNull(response.intensityDistribution());
        assertEquals(3, response.intensityDistribution().size());
        assertNotNull(response.progressObservations());
        assertTrue(response.progressObservations().isEmpty());
        assertNotNull(response.safetyNote());
        assertTrue(response.safetyNote().contains("self-reflection"));
    }

    @Test
    void getProgressAnalytics_MultipleUrgeLogs_CorrectAverageIntensity() {
        UrgeLog log1 = createUrgeLog("Stress", 8, LocalDateTime.now());
        UrgeLog log2 = createUrgeLog("Boredom", 4, LocalDateTime.now());
        UrgeLog log3 = createUrgeLog("Stress", 6, LocalDateTime.now());
        List<UrgeLog> logs = Arrays.asList(log1, log2, log3);

        when(urgeLogRepository.findByUser(testUser)).thenReturn(logs);
        when(exposureTaskRepository.findByUserOrderByDifficultyLevelAsc(testUser)).thenReturn(Collections.emptyList());
        when(compulsionDelayAttemptRepository.findByUser(testUser)).thenReturn(Collections.emptyList());
        when(urgeLogRepository.findByUserAndCreatedAtBetweenOrderByCreatedAtAsc(eq(testUser), any(), any()))
                .thenReturn(logs);

        ProgressAnalyticsResponse response = service.getProgressAnalytics();

        assertEquals(3, response.totalUrgeLogs());
        assertEquals(6.0, response.averageUrgeIntensity()); // (8+4+6)/3 = 6.0
        assertEquals(8, response.highestUrgeIntensity());
    }

    @Test
    void getProgressAnalytics_TriggerBreakdown_CountsCorrectly() {
        UrgeLog log1 = createUrgeLog("Stress", 5, LocalDateTime.now());
        UrgeLog log2 = createUrgeLog("Stress", 7, LocalDateTime.now());
        UrgeLog log3 = createUrgeLog("Boredom", 3, LocalDateTime.now());
        UrgeLog log4 = createUrgeLog("Stress", 6, LocalDateTime.now());
        List<UrgeLog> logs = Arrays.asList(log1, log2, log3, log4);

        when(urgeLogRepository.findByUser(testUser)).thenReturn(logs);
        when(exposureTaskRepository.findByUserOrderByDifficultyLevelAsc(testUser)).thenReturn(Collections.emptyList());
        when(compulsionDelayAttemptRepository.findByUser(testUser)).thenReturn(Collections.emptyList());
        when(urgeLogRepository.findByUserAndCreatedAtBetweenOrderByCreatedAtAsc(eq(testUser), any(), any()))
                .thenReturn(logs);

        ProgressAnalyticsResponse response = service.getProgressAnalytics();

        List<TriggerBreakdownResponse> breakdown = response.triggerBreakdown();
        assertEquals(2, breakdown.size());
        // Sorted descending by count: Stress first
        assertEquals("Stress", breakdown.get(0).trigger());
        assertEquals(3, breakdown.get(0).count());
        assertEquals("Boredom", breakdown.get(1).trigger());
        assertEquals(1, breakdown.get(1).count());
    }

    @Test
    void getProgressAnalytics_IntensityDistribution_GroupsCorrectly() {
        UrgeLog low1 = createUrgeLog("A", 1, LocalDateTime.now());
        UrgeLog low2 = createUrgeLog("B", 3, LocalDateTime.now());
        UrgeLog med1 = createUrgeLog("C", 5, LocalDateTime.now());
        UrgeLog high1 = createUrgeLog("D", 8, LocalDateTime.now());
        UrgeLog high2 = createUrgeLog("E", 10, LocalDateTime.now());
        List<UrgeLog> logs = Arrays.asList(low1, low2, med1, high1, high2);

        when(urgeLogRepository.findByUser(testUser)).thenReturn(logs);
        when(exposureTaskRepository.findByUserOrderByDifficultyLevelAsc(testUser)).thenReturn(Collections.emptyList());
        when(compulsionDelayAttemptRepository.findByUser(testUser)).thenReturn(Collections.emptyList());
        when(urgeLogRepository.findByUserAndCreatedAtBetweenOrderByCreatedAtAsc(eq(testUser), any(), any()))
                .thenReturn(logs);

        ProgressAnalyticsResponse response = service.getProgressAnalytics();

        List<IntensityDistributionResponse> dist = response.intensityDistribution();
        assertEquals(3, dist.size());
        assertEquals(2, dist.get(0).count()); // Low (1-3)
        assertEquals(1, dist.get(1).count()); // Medium (4-6)
        assertEquals(2, dist.get(2).count()); // High (7-10)
    }

    @Test
    void getProgressAnalytics_SevenDayTrend_ReturnsExpectedDatePoints() {
        LocalDate today = LocalDate.now();
        UrgeLog todayLog = createUrgeLog("Test", 5, today.atTime(10, 0));
        UrgeLog yesterdayLog = createUrgeLog("Test", 7, today.minusDays(1).atTime(14, 0));

        when(urgeLogRepository.findByUser(testUser)).thenReturn(Arrays.asList(todayLog, yesterdayLog));
        when(exposureTaskRepository.findByUserOrderByDifficultyLevelAsc(testUser)).thenReturn(Collections.emptyList());
        when(compulsionDelayAttemptRepository.findByUser(testUser)).thenReturn(Collections.emptyList());
        when(urgeLogRepository.findByUserAndCreatedAtBetweenOrderByCreatedAtAsc(eq(testUser), any(), any()))
                .thenReturn(Arrays.asList(yesterdayLog, todayLog));

        ProgressAnalyticsResponse response = service.getProgressAnalytics();

        List<TrendPointResponse> trend = response.sevenDayUrgeTrend();
        assertEquals(7, trend.size());

        // Verify dates cover last 7 days
        assertEquals(today.minusDays(6).toString(), trend.get(0).date());
        assertEquals(today.toString(), trend.get(6).date());

        // Verify today and yesterday have data, others are 0
        TrendPointResponse todayPoint = trend.get(6);
        assertEquals(1, todayPoint.count());
        assertEquals(5.0, todayPoint.averageIntensity());

        TrendPointResponse yesterdayPoint = trend.get(5);
        assertEquals(1, yesterdayPoint.count());
        assertEquals(7.0, yesterdayPoint.averageIntensity());

        // Day with no logs should be 0
        TrendPointResponse emptyDay = trend.get(0);
        assertEquals(0, emptyDay.count());
        assertEquals(0.0, emptyDay.averageIntensity());
    }

    @Test
    void getProgressAnalytics_ExposureAndDelayMetrics() {
        when(urgeLogRepository.findByUser(testUser)).thenReturn(Collections.emptyList());
        when(urgeLogRepository.findByUserAndCreatedAtBetweenOrderByCreatedAtAsc(eq(testUser), any(), any()))
                .thenReturn(Collections.emptyList());

        // Exposure tasks
        ExposureTask completed = createExposureTask(ExposureStatus.COMPLETED);
        ExposureTask pending1 = createExposureTask(ExposureStatus.PENDING);
        ExposureTask pending2 = createExposureTask(ExposureStatus.PENDING);
        ExposureTask inProgress = createExposureTask(ExposureStatus.IN_PROGRESS);
        when(exposureTaskRepository.findByUserOrderByDifficultyLevelAsc(testUser))
                .thenReturn(Arrays.asList(completed, pending1, pending2, inProgress));

        // Delay attempts
        CompulsionDelayAttempt success1 = createDelayAttempt(CompulsionDelayOutcome.SUCCESS, 10);
        CompulsionDelayAttempt success2 = createDelayAttempt(CompulsionDelayOutcome.SUCCESS, 20);
        CompulsionDelayAttempt cancelled = createDelayAttempt(CompulsionDelayOutcome.CANCELLED, null);
        CompulsionDelayAttempt failed = createDelayAttempt(CompulsionDelayOutcome.FAILED, 5);
        when(compulsionDelayAttemptRepository.findByUser(testUser))
                .thenReturn(Arrays.asList(success1, success2, cancelled, failed));

        ProgressAnalyticsResponse response = service.getProgressAnalytics();

        // Exposure
        assertEquals(4, response.totalExposureTasks());
        assertEquals(1, response.completedExposureTasks());
        assertEquals(2, response.pendingExposureTasks());

        // Delay
        assertEquals(4, response.totalDelayAttempts());
        assertEquals(2, response.completedDelayAttempts());
        assertEquals(1, response.cancelledDelayAttempts());
        assertEquals(11.7, response.averageDelayMinutes()); // (10+20+5)/3 = 11.666... → 11.7
    }

    // ---- Helper methods ----

    private UrgeLog createUrgeLog(String trigger, int intensity, LocalDateTime createdAt) {
        UrgeLog log = new UrgeLog();
        log.setTriggerText(trigger);
        log.setIntensityBefore(intensity);
        setField(log, "createdAt", createdAt);
        return log;
    }

    private ExposureTask createExposureTask(ExposureStatus status) {
        ExposureTask task = new ExposureTask();
        task.setStatus(status);
        task.setTitle("Test Task");
        task.setDifficultyLevel(ExposureDifficulty.LOW);
        return task;
    }

    private CompulsionDelayAttempt createDelayAttempt(CompulsionDelayOutcome outcome, Integer actualMinutes) {
        CompulsionDelayAttempt attempt = new CompulsionDelayAttempt();
        attempt.setOutcome(outcome);
        attempt.setPlannedDelayMinutes(15);
        attempt.setActualDelayMinutes(actualMinutes);
        return attempt;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
