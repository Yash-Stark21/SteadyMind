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

        WeeklySummaryResponse response = new WeeklySummaryResponse();
        response.setStartDate(start.toLocalDate());
        response.setEndDate(end.toLocalDate());
        response.setSafetyNote("This is for self-reflection only and is not medical advice or a diagnosis.");

        if (logs.isEmpty()) {
            response.setTotalUrgeLogs(0);
            response.setAverageIntensity(0.0);
            response.setMostCommonTrigger("N/A");
            response.setHighestRiskPeriod("N/A");
            response.setProgressObservations("No urge logs recorded this week. Start logging your moments to see progress.");
            response.setRecurringPatterns("N/A");
            response.setSuggestedNextSteps("Log an urge next time you feel one.");
            return response;
        }

        response.setTotalUrgeLogs(logs.size());

        double avgIntensity = logs.stream().mapToInt(UrgeLog::getIntensityBefore).average().orElse(0.0);
        response.setAverageIntensity(Math.round(avgIntensity * 10.0) / 10.0);

        String commonTrigger = logs.stream()
                .collect(Collectors.groupingBy(UrgeLog::getTriggerText, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Unknown");
        response.setMostCommonTrigger(commonTrigger);

        String commonPeriod = logs.stream()
                .collect(Collectors.groupingBy(this::getTimeOfDay, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Unknown");
        response.setHighestRiskPeriod(commonPeriod);

        AiCoachRequestDto req = new AiCoachRequestDto();
        req.setMessage("weekly summary");
        AiCoachResponseDto aiResponse = aiClient.generateResponse(req);

        String aiMessage = aiResponse.getUserFacingMessage();
        String[] parts = aiMessage.split("\\|");
        
        if (parts.length >= 3) {
            response.setProgressObservations(parts[0].replace("Progress:", "").trim());
            response.setRecurringPatterns(parts[1].replace("Patterns:", "").trim());
            response.setSuggestedNextSteps(parts[2].replace("Steps:", "").trim());
        } else {
            response.setProgressObservations(aiMessage);
            response.setRecurringPatterns("Consistency is key. Keep logging.");
            response.setSuggestedNextSteps("Review your most common triggers and plan small delays.");
        }

        // Safety override just in case AI returns diagnosis words
        if (aiMessage.toLowerCase().contains("diagnosis") || aiMessage.toLowerCase().contains("cured")) {
            response.setProgressObservations("Keep practicing delays and noting your triggers.");
            response.setRecurringPatterns("Focus on small steps.");
            response.setSuggestedNextSteps("Discuss your logs with your care provider.");
        }

        return response;
    }

    private String getTimeOfDay(UrgeLog log) {
        int hour = log.getCreatedAt().getHour();
        if (hour >= 5 && hour < 12) return "Morning";
        if (hour >= 12 && hour < 17) return "Afternoon";
        if (hour >= 17 && hour < 21) return "Evening";
        return "Night";
    }
}
