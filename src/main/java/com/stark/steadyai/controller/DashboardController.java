package com.stark.steadyai.controller;

import com.stark.steadyai.dto.ExposureTaskResponse;
import com.stark.steadyai.enums.ExposureStatus;
import com.stark.steadyai.service.ExposureTaskService;
import com.stark.steadyai.service.UrgeLogService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * MVC controller for the main dashboard page.
 * Displays summary cards and navigation for all features.
 */
@Controller
public class DashboardController {

    private final UrgeLogService urgeLogService;
    private final ExposureTaskService exposureTaskService;

    public DashboardController(UrgeLogService urgeLogService, ExposureTaskService exposureTaskService) {
        this.urgeLogService = urgeLogService;
        this.exposureTaskService = exposureTaskService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Moment log stats
        int totalUrgeLogs = urgeLogService.getAllUrgeLogs().size();

        // Growth challenge stats
        List<ExposureTaskResponse> allTasks = exposureTaskService.getAllExposureTasksForCurrentUser();
        int totalExposureTasks = allTasks.size();
        long completedExposureTasks = allTasks.stream()
                .filter(t -> t.status() == ExposureStatus.COMPLETED)
                .count();

        model.addAttribute("totalUrgeLogs", totalUrgeLogs);
        model.addAttribute("totalExposureTasks", totalExposureTasks);
        model.addAttribute("completedExposureTasks", completedExposureTasks);

        return "dashboard";
    }
}
