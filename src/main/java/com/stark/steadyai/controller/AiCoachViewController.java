package com.stark.steadyai.controller;

import com.stark.steadyai.dto.AiCoachRequestDto;
import com.stark.steadyai.dto.AiCoachResponseDto;
import com.stark.steadyai.entity.AiMessage;
import com.stark.steadyai.entity.User;
import com.stark.steadyai.repository.UserRepository;
import com.stark.steadyai.service.AiCoachService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import com.stark.steadyai.security.SecurityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * MVC controller for the AI Coach page.
 * Handles displaying the coach form, submitting messages,
 * and showing recent conversation history.
 *
 * All business logic is delegated to AiCoachService.
 */
@Controller
public class AiCoachViewController {

    private final AiCoachService aiCoachService;
    private final UserRepository userRepository;

    public AiCoachViewController(AiCoachService aiCoachService, UserRepository userRepository) {
        this.aiCoachService = aiCoachService;
        this.userRepository = userRepository;
    }

    /**
     * Display the AI Coach page with an empty message form and recent history.
     */
    @GetMapping("/ai-coach")
    public String showAiCoach(Model model, Authentication authentication) {
        User user = getUser(authentication);

        model.addAttribute("aiCoachRequest", new AiCoachRequestDto());
        model.addAttribute("recentMessages", aiCoachService.getRecentMessages(user));

        return "ai-coach";
    }

    /**
     * Process the submitted message through the AI Coach pipeline.
     * If validation fails, re-display the form with errors.
     * On success, display the AI response alongside recent history.
     */
    @PostMapping("/ai-coach")
    public String submitMessage(@Valid @ModelAttribute("aiCoachRequest") AiCoachRequestDto requestDto,
                                BindingResult bindingResult,
                                Model model,
                                Authentication authentication) {
        User user = getUser(authentication);

        if (bindingResult.hasErrors()) {
            model.addAttribute("recentMessages", aiCoachService.getRecentMessages(user));
            return "ai-coach";
        }

        // Delegate to service — all pipeline logic lives there
        AiCoachResponseDto responseDto = aiCoachService.processMessage(requestDto, user);
        model.addAttribute("coachResponse", responseDto);

        // Reload recent messages (now includes the one just saved)
        model.addAttribute("recentMessages", aiCoachService.getRecentMessages(user));

        return "ai-coach";
    }

    // =========================================================================
    // User Lookup
    // =========================================================================

    /**
     * Resolves the current User entity.
     * Uses the same demo-user pattern as other services in the project
     * until full Spring Security authentication is wired up.
     */
    private User getUser(Authentication authentication) {
        return SecurityUtils.getCurrentUser();
    }


}
