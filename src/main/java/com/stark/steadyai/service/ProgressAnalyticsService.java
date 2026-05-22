package com.stark.steadyai.service;

import com.stark.steadyai.dto.*;
import com.stark.steadyai.entity.CompulsionDelayAttempt;
import com.stark.steadyai.entity.ExposureTask;
import com.stark.steadyai.entity.UrgeLog;
import com.stark.steadyai.entity.User;
import com.stark.steadyai.enums.CompulsionDelayOutcome;
import com.stark.steadyai.enums.ExposureStatus;
import com.stark.steadyai.repository.CompulsionDelayAttemptRepository;
import com.stark.steadyai.repository.ExposureTaskRepository;
import com.stark.steadyai.repository.UrgeLogRepository;
import com.stark.steadyai.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service responsible for computing progress analytics from
 * urge logs, exposure tasks, and compulsion delay attempts.
 *
 * All observations are non-diagnostic and safe for self-reflection only.
 */
@Service
@Transactional(readOnly = true)
public class ProgressAnalyticsService {

    private static final String SAFETY_NOTE =
            "This dashboard is for self-reflection only and is not medical advice or a diagnosis.";

    private final UrgeLogRepository urgeLogRepository;
    private final ExposureTaskRepository exposureTaskRepository;
    private final CompulsionDelayAttemptRepository compulsionDelayAttemptRepository;
    private final UserRepository userRepository;

    public ProgressAnalyticsService(UrgeLogRepository urgeLogRepository,
                                    ExposureTaskRepository exposureTaskRepository,
                                    CompulsionDelayAttemptRepository compulsionDelayAttemptRepository,
                                    UserRepository userRepository) {
        this.urgeLogRepository = urgeLogRepository;
        this.exposureTaskRepository = exposureTaskRepository;
        this.compulsionDelayAttemptRepository = compulsionDelayAttemptRepository;
        this.userRepository = userRepository;
    }

    /**
     * Builds the full progress analytics response for the current user.
     */
    public ProgressAnalyticsResponse getProgressAnalytics() {
        User user = getDemoUser();

        List<UrgeLog> allUrgeLogs = urgeLogRepository.findByUser(user);
        List<ExposureTask> allExposureTasks = exposureTaskRepository.findByUserOrderByDifficultyLevelAsc(user);
        List<CompulsionDelayAttempt> allDelayAttempts = compulsionDelayAttemptRepository.findByUser(user);

        ProgressAnalyticsResponse response = new ProgressAnalyticsResponse();

        // Urge metrics
        buildUrgeMetrics(response, allUrgeLogs);

        // Exposure metrics
        buildExposureMetrics(response, allExposureTasks);

        // Delay metrics
        buildDelayMetrics(response, allDelayAttempts);

        // 7-day trend
        response.setSevenDayUrgeTrend(buildSevenDayTrend(user));

        // Trigger breakdown
        response.setTriggerBreakdown(buildTriggerBreakdown(allUrgeLogs));

        // Intensity distribution
        response.setIntensityDistribution(buildIntensityDistribution(allUrgeLogs));

        // Progress observations
        response.setProgressObservations(
                buildProgressObservations(allUrgeLogs, allExposureTasks, allDelayAttempts, user));

        // Safety note
        response.setSafetyNote(SAFETY_NOTE);

        return response;
    }

    // ---- Urge Metrics ----

    private void buildUrgeMetrics(ProgressAnalyticsResponse response, List<UrgeLog> logs) {
        response.setTotalUrgeLogs(logs.size());

        if (logs.isEmpty()) {
            response.setAverageUrgeIntensity(0.0);
            response.setHighestUrgeIntensity(0);
            return;
        }

        double avgIntensity = logs.stream()
                .filter(log -> log.getIntensityBefore() != null)
                .mapToInt(UrgeLog::getIntensityBefore)
                .average()
                .orElse(0.0);
        response.setAverageUrgeIntensity(Math.round(avgIntensity * 10.0) / 10.0);

        int maxIntensity = logs.stream()
                .filter(log -> log.getIntensityBefore() != null)
                .mapToInt(UrgeLog::getIntensityBefore)
                .max()
                .orElse(0);
        response.setHighestUrgeIntensity(maxIntensity);
    }

    // ---- Exposure Metrics ----

    private void buildExposureMetrics(ProgressAnalyticsResponse response, List<ExposureTask> tasks) {
        response.setTotalExposureTasks(tasks.size());

        long completed = tasks.stream()
                .filter(t -> t.getStatus() == ExposureStatus.COMPLETED)
                .count();
        response.setCompletedExposureTasks((int) completed);

        long pending = tasks.stream()
                .filter(t -> t.getStatus() == ExposureStatus.PENDING)
                .count();
        response.setPendingExposureTasks((int) pending);
    }

    // ---- Delay Metrics ----

    private void buildDelayMetrics(ProgressAnalyticsResponse response, List<CompulsionDelayAttempt> attempts) {
        response.setTotalDelayAttempts(attempts.size());

        long completedCount = attempts.stream()
                .filter(a -> a.getOutcome() == CompulsionDelayOutcome.SUCCESS)
                .count();
        response.setCompletedDelayAttempts((int) completedCount);

        long cancelledCount = attempts.stream()
                .filter(a -> a.getOutcome() == CompulsionDelayOutcome.CANCELLED)
                .count();
        response.setCancelledDelayAttempts((int) cancelledCount);

        double avgDelay = attempts.stream()
                .filter(a -> a.getActualDelayMinutes() != null)
                .mapToInt(CompulsionDelayAttempt::getActualDelayMinutes)
                .average()
                .orElse(0.0);
        response.setAverageDelayMinutes(Math.round(avgDelay * 10.0) / 10.0);
    }

    // ---- 7-Day Trend ----

    private List<TrendPointResponse> buildSevenDayTrend(User user) {
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(6); // includes today = 7 days

        LocalDateTime start = sevenDaysAgo.atStartOfDay();
        LocalDateTime end = today.atTime(LocalTime.MAX);

        List<UrgeLog> recentLogs = urgeLogRepository
                .findByUserAndCreatedAtBetweenOrderByCreatedAtAsc(user, start, end);

        // Group logs by date
        Map<LocalDate, List<UrgeLog>> logsByDate = recentLogs.stream()
                .filter(log -> log.getCreatedAt() != null)
                .collect(Collectors.groupingBy(log -> log.getCreatedAt().toLocalDate()));

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        List<TrendPointResponse> trend = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate date = sevenDaysAgo.plusDays(i);
            List<UrgeLog> dayLogs = logsByDate.getOrDefault(date, Collections.emptyList());

            int count = dayLogs.size();
            double avgIntensity = dayLogs.stream()
                    .filter(log -> log.getIntensityBefore() != null)
                    .mapToInt(UrgeLog::getIntensityBefore)
                    .average()
                    .orElse(0.0);

            trend.add(new TrendPointResponse(
                    date.format(formatter),
                    count,
                    Math.round(avgIntensity * 10.0) / 10.0
            ));
        }

        return trend;
    }

    // ---- Trigger Breakdown ----

    private List<TriggerBreakdownResponse> buildTriggerBreakdown(List<UrgeLog> logs) {
        Map<String, Long> triggerCounts = logs.stream()
                .filter(log -> log.getTriggerText() != null && !log.getTriggerText().isBlank())
                .collect(Collectors.groupingBy(UrgeLog::getTriggerText, Collectors.counting()));

        return triggerCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(entry -> new TriggerBreakdownResponse(entry.getKey(), entry.getValue().intValue()))
                .collect(Collectors.toList());
    }

    // ---- Intensity Distribution ----

    private List<IntensityDistributionResponse> buildIntensityDistribution(List<UrgeLog> logs) {
        int low = 0, medium = 0, high = 0;

        for (UrgeLog log : logs) {
            if (log.getIntensityBefore() == null) continue;
            int intensity = log.getIntensityBefore();
            if (intensity >= 1 && intensity <= 3) {
                low++;
            } else if (intensity >= 4 && intensity <= 6) {
                medium++;
            } else if (intensity >= 7 && intensity <= 10) {
                high++;
            }
        }

        List<IntensityDistributionResponse> distribution = new ArrayList<>();
        distribution.add(new IntensityDistributionResponse("Low (1–3)", low));
        distribution.add(new IntensityDistributionResponse("Medium (4–6)", medium));
        distribution.add(new IntensityDistributionResponse("High (7–10)", high));
        return distribution;
    }

    // ---- Progress Observations ----

    private List<String> buildProgressObservations(List<UrgeLog> urgeLogs,
                                                    List<ExposureTask> exposureTasks,
                                                    List<CompulsionDelayAttempt> delayAttempts,
                                                    User user) {
        List<String> observations = new ArrayList<>();

        if (urgeLogs.isEmpty() && exposureTasks.isEmpty() && delayAttempts.isEmpty()) {
            return observations;
        }

        // Logging consistency in last 7 days
        if (!urgeLogs.isEmpty()) {
            LocalDate today = LocalDate.now();
            LocalDateTime weekStart = today.minusDays(6).atStartOfDay();
            long daysWithLogs = urgeLogs.stream()
                    .filter(log -> log.getCreatedAt() != null && log.getCreatedAt().isAfter(weekStart))
                    .map(log -> log.getCreatedAt().toLocalDate())
                    .distinct()
                    .count();

            if (daysWithLogs > 0) {
                observations.add("You logged urges on " + daysWithLogs + " of the last 7 days, which shows consistency.");
            }
        }

        // Most common trigger
        if (!urgeLogs.isEmpty()) {
            urgeLogs.stream()
                    .filter(log -> log.getTriggerText() != null && !log.getTriggerText().isBlank())
                    .collect(Collectors.groupingBy(UrgeLog::getTriggerText, Collectors.counting()))
                    .entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .ifPresent(entry ->
                            observations.add("Your most common trigger was \"" + entry.getKey() + "\".")
                    );
        }

        // Completed exposure tasks
        long completedExposures = exposureTasks.stream()
                .filter(t -> t.getStatus() == ExposureStatus.COMPLETED)
                .count();
        if (completedExposures > 0) {
            observations.add("You completed " + completedExposures + " exposure task" +
                    (completedExposures > 1 ? "s" : "") + ".");
        }

        // Delay attempt completion rate
        if (!delayAttempts.isEmpty()) {
            long successfulDelays = delayAttempts.stream()
                    .filter(a -> a.getOutcome() == CompulsionDelayOutcome.SUCCESS)
                    .count();
            observations.add("You completed " + successfulDelays + " out of " +
                    delayAttempts.size() + " delay attempt" +
                    (delayAttempts.size() > 1 ? "s" : "") + ".");
        }

        return observations;
    }

    // ---- User Resolution ----

    private User getDemoUser() {
        return userRepository.findByEmail("demo@steadyai.local")
                .orElseGet(() -> {
                    User newUser = new User("Demo User", "demo@steadyai.local", "TEMP_PASSWORD_HASH");
                    return userRepository.save(newUser);
                });
    }
}
