package com.stark.steadyai.controller;

import com.stark.steadyai.dto.ExposureTaskRequest;
import com.stark.steadyai.dto.ExposureTaskResponse;
import com.stark.steadyai.enums.ExposureDifficulty;
import com.stark.steadyai.service.ExposureTaskService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * MVC controller for Exposure Task (Growth Challenge) pages.
 * Provides list view, form creation, and form submission with validation.
 */
@Controller
@RequestMapping("/exposures")
public class ExposureViewController {

    private final ExposureTaskService exposureTaskService;

    public ExposureViewController(ExposureTaskService exposureTaskService) {
        this.exposureTaskService = exposureTaskService;
    }

    /**
     * Display all growth challenges for the current user.
     */
    @GetMapping
    public String listExposures(Model model) {
        List<ExposureTaskResponse> tasks = exposureTaskService.getAllExposureTasksForCurrentUser();
        model.addAttribute("tasks", tasks);
        return "exposure-list";
    }

    /**
     * Show the empty form to create a new growth challenge.
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("exposureTaskRequest", new ExposureTaskRequest());
        model.addAttribute("difficultyLevels", ExposureDifficulty.values());
        return "exposure-form";
    }

    /**
     * Handle form submission. If validation fails, re-display form with errors.
     * On success, redirect to the list page.
     */
    @PostMapping
    public String createExposure(@Valid @ModelAttribute("exposureTaskRequest") ExposureTaskRequest request,
                                 BindingResult bindingResult,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("difficultyLevels", ExposureDifficulty.values());
            return "exposure-form";
        }
        exposureTaskService.createExposureTask(request);
        return "redirect:/exposures";
    }
}
