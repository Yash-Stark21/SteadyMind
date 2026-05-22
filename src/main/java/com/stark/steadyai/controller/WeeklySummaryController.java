package com.stark.steadyai.controller;

import com.stark.steadyai.dto.WeeklySummaryResponse;
import com.stark.steadyai.service.WeeklySummaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class WeeklySummaryController {

    private final WeeklySummaryService weeklySummaryService;

    public WeeklySummaryController(WeeklySummaryService weeklySummaryService) {
        this.weeklySummaryService = weeklySummaryService;
    }

    @GetMapping("/weekly-summary")
    public ResponseEntity<WeeklySummaryResponse> getWeeklySummary() {
        WeeklySummaryResponse response = weeklySummaryService.getWeeklySummary();
        return ResponseEntity.ok(response);
    }
}
