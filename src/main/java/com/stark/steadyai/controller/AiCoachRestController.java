package com.stark.steadyai.controller;

import com.stark.steadyai.dto.AiCoachRequestDto;
import com.stark.steadyai.dto.AiCoachResponseDto;
import com.stark.steadyai.entity.User;
import com.stark.steadyai.repository.UserRepository;
import com.stark.steadyai.service.AiCoachService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for the AI Coach pipeline.
 *
 * Exposes a JSON API for the AI Coach, separate from the Thymeleaf MVC flow.
 * Both endpoints delegate to the same AiCoachService.
 */
@RestController
@RequestMapping("/api/ai")
public class AiCoachRestController {

    private final AiCoachService aiCoachService;
    private final UserRepository userRepository;

    public AiCoachRestController(AiCoachService aiCoachService, UserRepository userRepository) {
        this.aiCoachService = aiCoachService;
        this.userRepository = userRepository;
    }

    /**
     * Process a user message through the AI Coach pipeline and return
     * a structured JSON response.
     *
     * @param requestDto the validated user message
     * @return the AI Coach response with intent, risk, and suggested action
     */
    @PostMapping("/coach")
    public ResponseEntity<AiCoachResponseDto> coach(
            @Valid @RequestBody AiCoachRequestDto requestDto,
            Authentication authentication) {

        User user = getUser(authentication);
        AiCoachResponseDto responseDto = aiCoachService.processMessage(requestDto, user);
        return ResponseEntity.ok(responseDto);
    }

    // =========================================================================
    // User Lookup (same pattern as AiCoachViewController)
    // =========================================================================

    private User getUser(Authentication authentication) {
        if (authentication != null && authentication.getName() != null) {
            return userRepository.findByEmail(authentication.getName())
                    .orElseGet(this::getDemoUser);
        }
        return getDemoUser();
    }

    private User getDemoUser() {
        return userRepository.findByEmail("demo@steadyai.local")
                .orElseGet(() -> {
                    User newUser = new User("Demo User", "demo@steadyai.local", "TEMP_PASSWORD_HASH");
                    return userRepository.save(newUser);
                });
    }
}
