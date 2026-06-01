package com.stark.steadyai.service;

import com.stark.steadyai.dto.ProgressAnalyticsResponse;
import com.stark.steadyai.dto.ProgressReportResponse;
import com.stark.steadyai.dto.WeeklySummaryResponse;
import com.stark.steadyai.entity.User;
import com.stark.steadyai.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProgressReportService {

    private static final String SAFETY_NOTE = "This is for self-reflection only and is not medical advice or a diagnosis.";
    
    private final ProgressAnalyticsService progressAnalyticsService;
    private final WeeklySummaryService weeklySummaryService;
    private final UserRepository userRepository;
    private final TemplateEngine templateEngine;

    public ProgressReportService(ProgressAnalyticsService progressAnalyticsService,
                                 WeeklySummaryService weeklySummaryService,
                                 UserRepository userRepository,
                                 TemplateEngine templateEngine) {
        this.progressAnalyticsService = progressAnalyticsService;
        this.weeklySummaryService = weeklySummaryService;
        this.userRepository = userRepository;
        this.templateEngine = templateEngine;
    }

    public ProgressReportResponse generateReport(User user, int days) {
        ProgressAnalyticsResponse analytics = progressAnalyticsService.getProgressAnalytics();
        WeeklySummaryResponse weeklySummary = weeklySummaryService.getWeeklySummary();

        String generatedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String startDate = LocalDate.now().minusDays(days - 1).toString();
        String endDate = LocalDate.now().toString();
        String userDisplayName = user != null ? user.getName() : "Demo User";

        String mostCommonTrigger = "N/A";
        if (analytics.triggerBreakdown() != null && !analytics.triggerBreakdown().isEmpty()) {
            mostCommonTrigger = analytics.triggerBreakdown().get(0).trigger();
        }

        String weeklySummaryText;
        List<String> keyObservations;
        List<String> suggestedNextSteps;

        if (analytics.totalUrgeLogs() == 0 && analytics.totalExposureTasks() == 0 && analytics.totalDelayAttempts() == 0) {
            weeklySummaryText = "No progress data was found for this period. Start logging urges, delay attempts, and exposure tasks to generate a meaningful report.";
            keyObservations = new ArrayList<>();
            suggestedNextSteps = new ArrayList<>();
        } else {
            weeklySummaryText = weeklySummary.progressObservations();
            keyObservations = analytics.progressObservations();
            List<String> nextSteps = new ArrayList<>();
            if (weeklySummary.suggestedNextSteps() != null) {
                nextSteps.add(weeklySummary.suggestedNextSteps());
            }
            suggestedNextSteps = nextSteps;
        }

        return new ProgressReportResponse(
                generatedAt, startDate, endDate, userDisplayName,
                analytics.totalUrgeLogs(), analytics.averageUrgeIntensity(), mostCommonTrigger,
                analytics.completedExposureTasks(), analytics.pendingExposureTasks(),
                analytics.completedDelayAttempts(), analytics.averageDelayMinutes(),
                weeklySummaryText, keyObservations, suggestedNextSteps, SAFETY_NOTE
        );
    }

    public String generateHtmlReport(User user, int days) {
        ProgressReportResponse reportData = generateReport(user, days);
        Context context = new Context();
        context.setVariable("report", reportData);
        return templateEngine.process("progress-report-export", context);
    }
}
