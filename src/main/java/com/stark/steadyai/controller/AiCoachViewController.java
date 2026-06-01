package com.stark.steadyai.controller;

import com.stark.steadyai.dto.AiCoachRequestDto;
import com.stark.steadyai.entity.User;
import com.stark.steadyai.enums.Role;
import com.stark.steadyai.security.SecurityUtils;
import com.stark.steadyai.service.AiCoachService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * MVC controller for the AI Coach page.
 */
@Controller
public class AiCoachViewController {

    private final AiCoachService aiCoachService;

    public AiCoachViewController(AiCoachService aiCoachService) {
        this.aiCoachService = aiCoachService;
    }

    @GetMapping("/ai-coach")
    public String showAiCoach(Model model, Authentication authentication) {
        User user = getUser(authentication);
        populateCoachPage(model, user);

        return "ai-coach";
    }

    @PostMapping("/ai-coach")
    public String submitMessage(@Valid @ModelAttribute("aiCoachRequest") AiCoachRequestDto requestDto,
                                BindingResult bindingResult,
                                Model model,
                                Authentication authentication) {
        User user = getUser(authentication);

        if (!bindingResult.hasErrors()) {
            aiCoachService.processMessage(requestDto, user);
        }

        populateCoachPage(model, user);
        return "ai-coach";
    }

    private void populateCoachPage(Model model, User user) {
        model.addAttribute("aiCoachRequest", new AiCoachRequestDto(null));
        model.addAttribute("activeMessages", aiCoachService.getActiveConversationMessages(user));
        model.addAttribute("recentConversations", aiCoachService.getRecentConversationSummaries(user));
        model.addAttribute("showClinicalMetadata", user != null && user.getRole() == Role.ROLE_THERAPIST);
    }

    private User getUser(Authentication authentication) {
        return SecurityUtils.getCurrentUser();
    }
}
