package com.stark.steadyai.controller;

import com.stark.steadyai.dto.UrgeLogRequest;
import com.stark.steadyai.dto.UrgeLogResponse;
import com.stark.steadyai.service.UrgeLogService;
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
 * MVC controller for Urge Log (Moment Log) pages.
 * Provides list view, form creation, and form submission with validation.
 */
@Controller
@RequestMapping("/urge-logs")
public class UrgeLogViewController {

    private final UrgeLogService urgeLogService;

    public UrgeLogViewController(UrgeLogService urgeLogService) {
        this.urgeLogService = urgeLogService;
    }

    /**
     * Display all moment logs for the current user.
     */
    @GetMapping
    public String listUrgeLogs(Model model) {
        List<UrgeLogResponse> logs = urgeLogService.getAllUrgeLogs();
        model.addAttribute("logs", logs);
        return "urge-log-list";
    }

    /**
     * Show the empty form to create a new moment log.
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("urgeLogRequest", new UrgeLogRequest());
        return "urge-log-form";
    }

    /**
     * Handle form submission. If validation fails, re-display form with errors.
     * On success, redirect to the list page.
     */
    @PostMapping
    public String createUrgeLog(@Valid @ModelAttribute("urgeLogRequest") UrgeLogRequest request,
                                BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "urge-log-form";
        }
        urgeLogService.createUrgeLog(request);
        return "redirect:/urge-logs";
    }
}
