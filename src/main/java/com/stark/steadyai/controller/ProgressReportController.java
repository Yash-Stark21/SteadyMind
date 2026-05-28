package com.stark.steadyai.controller;

import com.stark.steadyai.dto.ProgressReportResponse;
import com.stark.steadyai.entity.User;
import com.stark.steadyai.repository.UserRepository;
import com.stark.steadyai.service.ProgressReportService;
import com.stark.steadyai.security.SecurityUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports/progress")
public class ProgressReportController {

    private final ProgressReportService progressReportService;
    private final UserRepository userRepository;

    public ProgressReportController(ProgressReportService progressReportService, UserRepository userRepository) {
        this.progressReportService = progressReportService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<ProgressReportResponse> getProgressReport(Authentication authentication) {
        User user = getUser(authentication);
        ProgressReportResponse response = progressReportService.generateReport(user, 7);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadProgressReport(Authentication authentication) {
        User user = getUser(authentication);
        String htmlContent = progressReportService.generateHtmlReport(user, 7);
        
        String filename = "steadyai-progress-report-" + LocalDate.now() + ".html";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);
        headers.setContentDispositionFormData("attachment", filename);

        return ResponseEntity.ok()
                .headers(headers)
                .body(htmlContent.getBytes(StandardCharsets.UTF_8));
    }

    private User getUser(Authentication authentication) {
        return SecurityUtils.getCurrentUser();
    }


}
