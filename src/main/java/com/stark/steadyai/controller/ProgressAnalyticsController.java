package com.stark.steadyai.controller;

import com.stark.steadyai.dto.ProgressAnalyticsResponse;
import com.stark.steadyai.service.ProgressAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for progress analytics data.
 * Consumed by the Thymeleaf progress page via JavaScript fetch.
 */
@RestController
@RequestMapping("/api/progress")
public class ProgressAnalyticsController {

    private final ProgressAnalyticsService progressAnalyticsService;

    public ProgressAnalyticsController(ProgressAnalyticsService progressAnalyticsService) {
        this.progressAnalyticsService = progressAnalyticsService;
    }

    @GetMapping("/analytics")
    public ResponseEntity<ProgressAnalyticsResponse> getAnalytics() {
        ProgressAnalyticsResponse response = progressAnalyticsService.getProgressAnalytics();
        return ResponseEntity.ok(response);
    }
}
