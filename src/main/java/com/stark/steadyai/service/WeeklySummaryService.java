package com.stark.steadyai.service;

import com.stark.steadyai.ai.AiClient;
import com.stark.steadyai.dto.AiCoachRequestDto;
import com.stark.steadyai.dto.AiCoachResponseDto;
import com.stark.steadyai.dto.WeeklySummaryResponse;
import com.stark.steadyai.entity.UrgeLog;
import com.stark.steadyai.entity.User;
import com.stark.steadyai.repository.UrgeLogRepository;
import com.stark.steadyai.repository.UserRepository;
import com.stark.steadyai.security.SecurityUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WeeklySummaryService {

    private final UrgeLogRepository urgeLogRepository;
    private final UserRepository userRepository;
    private final AiClient aiClient;

    public WeeklySummaryService(UrgeLogRepository urgeLogRepository, UserRepository userRepository, AiClient aiClient) {
        this.urgeLogRepository = urgeLogRepository;
        this.userRepository = userRepository;
        this.aiClient = aiClient;
    }



    public WeeklySummaryResponse getWeeklySummary() {
        User user = SecurityUtils.getCurrentUser();
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(7);

        List<UrgeLog> logs = urgeLogRepository.findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(user, start, end);

        LocalDate startDate = start.toLocalDate();
        LocalDate endDate = end.toLocalDate();
        String safetyNote = "This is for self-reflection only and is not medical advice or a diagnosis.";

        if (logs.isEmpty()) {
            return new WeeklySummaryResponse(
                    startDate, endDate, 0, 0.0, "N/A", "N/A",
                    "No urge logs recorded this week. Start logging your moments to see progress.",
                    "N/A", "Log an urge next time you feel one.", safetyNote
            );
        }

        int totalUrgeLogs = logs.size();
        double averageIntensity = Math.round(logs.stream().mapToInt(UrgeLog::getIntensityBefore).average().orElse(0.0) * 10.0) / 10.0;

        String mostCommonTrigger = logs.stream()
                .collect(Collectors.groupingBy(UrgeLog::getTriggerText, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Unknown");

        String highestRiskPeriod = logs.stream()
                .collect(Collectors.groupingBy(this::getTimeOfDay, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Unknown");

        AiCoachRequestDto req = new AiCoachRequestDto("weekly summary");
        AiCoachResponseDto aiResponse = aiClient.generateResponse(req);

        String aiMessage = aiResponse.getUserFacingMessage();
        String[] parts = aiMessage.split("\\|");

        String progressObservations;
        String recurringPatterns;
        String suggestedNextSteps;

        if (parts.length >= 3) {
            progressObservations = parts[0].replace("Progress:", "").trim();
            recurringPatterns = parts[1].replace("Patterns:", "").trim();
            suggestedNextSteps = parts[2].replace("Steps:", "").trim();
        } else {
            progressObservations = aiMessage;
            recurringPatterns = "Consistency is key. Keep logging.";
            suggestedNextSteps = "Review your most common triggers and plan small delays.";
        }

        // Safety override just in case AI returns diagnosis words
        if (aiMessage.toLowerCase().contains("diagnosis") || aiMessage.toLowerCase().contains("cured")) {
            progressObservations = "Keep practicing delays and noting your triggers.";
            recurringPatterns = "Focus on small steps.";
            suggestedNextSteps = "Discuss your logs with your care provider.";
        }

        return new WeeklySummaryResponse(
                startDate, endDate, totalUrgeLogs, averageIntensity,
                mostCommonTrigger, highestRiskPeriod, progressObservations,
                recurringPatterns, suggestedNextSteps, safetyNote
        );
    }

    private String getTimeOfDay(UrgeLog log) {
        int hour = log.getCreatedAt().getHour();
        if (hour >= 5 && hour < 12) return "Morning";
        if (hour >= 12 && hour < 17) return "Afternoon";
        if (hour >= 17 && hour < 21) return "Evening";
        return "Night";
    }
}
