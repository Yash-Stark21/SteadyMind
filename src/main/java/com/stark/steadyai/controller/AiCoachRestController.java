package com.stark.steadyai.controller;

import com.stark.steadyai.dto.AiCoachExchangeResponseDto;
import com.stark.steadyai.dto.AiCoachRequestDto;
import com.stark.steadyai.dto.AiConversationSummaryDto;
import com.stark.steadyai.entity.User;
import com.stark.steadyai.enums.Role;
import com.stark.steadyai.security.SecurityUtils;
import com.stark.steadyai.service.AiCoachService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for the AI Coach pipeline.
 */
@RestController
@RequestMapping("/api/ai")
public class AiCoachRestController {

    private final AiCoachService aiCoachService;

    public AiCoachRestController(AiCoachService aiCoachService) {
        this.aiCoachService = aiCoachService;
    }

    @PostMapping("/coach")
    public ResponseEntity<AiCoachExchangeResponseDto> coach(
            @Valid @RequestBody AiCoachRequestDto requestDto,
            Authentication authentication) {

        User user = getUser(authentication);
        AiCoachExchangeResponseDto responseDto = aiCoachService.processMessage(requestDto, user);
        return ResponseEntity.ok(applyRoleVisibility(responseDto, user));
    }

    @PostMapping("/conversations/new")
    public ResponseEntity<AiConversationSummaryDto> startNewConversation(Authentication authentication) {
        User user = getUser(authentication);
        AiConversationSummaryDto summary = aiCoachService.startNewConversation(user);

        if (summary == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<List<AiCoachExchangeResponseDto>> getConversationMessages(
            @PathVariable Long conversationId,
            Authentication authentication) {

        User user = getUser(authentication);
        List<AiCoachExchangeResponseDto> messages = aiCoachService.getConversationMessages(conversationId, user).stream()
                .map(message -> applyRoleVisibility(message, user))
                .toList();

        return ResponseEntity.ok(messages);
    }

    private AiCoachExchangeResponseDto applyRoleVisibility(AiCoachExchangeResponseDto responseDto, User user) {
        if (isTherapist(user)) {
            return responseDto;
        }

        return new AiCoachExchangeResponseDto(
                responseDto.conversationId(),
                responseDto.userMessage(),
                responseDto.aiResponse(),
                null,
                null,
                null,
                responseDto.createdAt()
        );
    }

    private boolean isTherapist(User user) {
        return user != null && user.getRole() == Role.ROLE_THERAPIST;
    }

    private User getUser(Authentication authentication) {
        return SecurityUtils.getCurrentUser();
    }
}
