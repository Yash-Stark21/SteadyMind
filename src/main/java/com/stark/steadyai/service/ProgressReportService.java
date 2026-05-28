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

    private static final String SAFETY_NOTE = "This report is for personal reflection only and is not medical advice or a diagnosis.";
    
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
        // We reuse the existing services. Currently they fetch for the demo user internally, 
        // but since auth is partially implemented, we'll build the report using the available data.
        
        ProgressAnalyticsResponse analytics = progressAnalyticsService.getProgressAnalytics();
        WeeklySummaryResponse weeklySummary = weeklySummaryService.getWeeklySummary();

        ProgressReportResponse report = new ProgressReportResponse();
        report.setGeneratedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        report.setStartDate(LocalDate.now().minusDays(days - 1).toString());
        report.setEndDate(LocalDate.now().toString());
        report.setUserDisplayName(user != null ? user.getName() : "Demo User");

        report.setTotalUrgeLogs(analytics.getTotalUrgeLogs());
        report.setAverageUrgeIntensity(analytics.getAverageUrgeIntensity());
        
        String mostCommonTrigger = "N/A";
        if (analytics.getTriggerBreakdown() != null && !analytics.getTriggerBreakdown().isEmpty()) {
            mostCommonTrigger = analytics.getTriggerBreakdown().get(0).getTrigger();
        }
        report.setMostCommonTrigger(mostCommonTrigger);

        report.setCompletedExposureTasks(analytics.getCompletedExposureTasks());
        report.setPendingExposureTasks(analytics.getPendingExposureTasks());
        report.setCompletedDelayAttempts(analytics.getCompletedDelayAttempts());
        report.setAverageDelayMinutes(analytics.getAverageDelayMinutes());

        if (analytics.getTotalUrgeLogs() == 0 && analytics.getTotalExposureTasks() == 0 && analytics.getTotalDelayAttempts() == 0) {
            report.setWeeklySummary("No progress data was found for this period. Start logging urges, delay attempts, and exposure tasks to generate a meaningful report.");
            report.setKeyObservations(new ArrayList<>());
            report.setSuggestedNextSteps(new ArrayList<>());
        } else {
            report.setWeeklySummary(weeklySummary.getProgressObservations());
            report.setKeyObservations(analytics.getProgressObservations());
            
            List<String> nextSteps = new ArrayList<>();
            if (weeklySummary.getSuggestedNextSteps() != null) {
                nextSteps.add(weeklySummary.getSuggestedNextSteps());
            }
            report.setSuggestedNextSteps(nextSteps);
        }

        report.setSafetyNote(SAFETY_NOTE);

        return report;
    }

    public String generateHtmlReport(User user, int days) {
        ProgressReportResponse reportData = generateReport(user, days);
        Context context = new Context();
        context.setVariable("report", reportData);
        return templateEngine.process("progress-report-export", context);
    }
}
