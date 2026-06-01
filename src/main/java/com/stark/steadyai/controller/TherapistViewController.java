package com.stark.steadyai.controller;

import com.stark.steadyai.entity.User;
import com.stark.steadyai.security.SecurityUtils;
import com.stark.steadyai.service.TherapistService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@PreAuthorize("hasAuthority('ROLE_THERAPIST')")
public class TherapistViewController {

    private final TherapistService therapistService;

    public TherapistViewController(TherapistService therapistService) {
        this.therapistService = therapistService;
    }

    @GetMapping("/therapist")
    public String dashboard(Model model) {
        User therapist = SecurityUtils.getCurrentUser();
        model.addAttribute("patients", therapistService.getAssignedPatientSummaries(therapist));

        return "therapist-dashboard";
    }

    @GetMapping("/therapist/patients/{patientId}")
    public String patientDetail(@PathVariable Long patientId, Model model) {
        User therapist = SecurityUtils.getCurrentUser();
        User patient = therapistService.requireAssignedPatient(patientId, therapist);

        model.addAttribute("patient", patient);
        model.addAttribute("summary", therapistService.getPatientSummary(patient));
        model.addAttribute("urgeLogs", therapistService.getPatientUrgeLogs(patient));
        model.addAttribute("exposureTasks", therapistService.getPatientExposureTasks(patient));
        model.addAttribute("delayAttempts", therapistService.getPatientDelayAttempts(patient));
        model.addAttribute("aiConversations", therapistService.getPatientAiConversations(patient));

        return "therapist-patient-detail";
    }
}
