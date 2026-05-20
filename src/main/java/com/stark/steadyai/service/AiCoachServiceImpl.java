package com.stark.steadyai.service;

import com.stark.steadyai.dto.AiCoachRequestDto;
import com.stark.steadyai.dto.AiCoachResponseDto;
import com.stark.steadyai.entity.AiMessage;
import com.stark.steadyai.entity.User;
import com.stark.steadyai.enums.CoachIntent;
import com.stark.steadyai.enums.ResponseType;
import com.stark.steadyai.enums.RiskLevel;
import com.stark.steadyai.repository.AiMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of the controlled AI Coach pipeline.
 *
 * Pipeline flow:
 * 1. Extract user message
 * 2. Detect CoachIntent (keyword-based, rule-driven)
 * 3. Classify RiskLevel (keyword-based, rule-driven)
 * 4. Select ResponseType (mapping from intent + risk)
 * 5. Generate controlled mock response
 * 6. Save AiMessage audit record
 * 7. Return AiCoachResponseDto
 *
 * No real AI/LLM is invoked. All responses are deterministic and auditable.
 */
@Service
@Transactional
public class AiCoachServiceImpl implements AiCoachService {

    private final AiMessageRepository aiMessageRepository;

    public AiCoachServiceImpl(AiMessageRepository aiMessageRepository) {
        this.aiMessageRepository = aiMessageRepository;
    }

    // =========================================================================
    // Public API
    // =========================================================================

    @Override
    public AiCoachResponseDto processMessage(AiCoachRequestDto requestDto, User user) {
        String userMessage = requestDto.getMessage().trim();

        // Step 1-2: Classify risk first (safety takes priority)
        RiskLevel riskLevel = detectRiskLevel(userMessage);

        // Step 3: Detect intent
        CoachIntent intent = detectIntent(userMessage, riskLevel);

        // Step 4: Select response type
        ResponseType responseType = selectResponseType(intent, riskLevel);

        // Step 5: Generate controlled response
        String aiResponse = generateResponse(intent, riskLevel, responseType);

        // Step 6: Save audit record
        AiMessage aiMessage = new AiMessage();
        aiMessage.setUser(user);
        aiMessage.setUserMessage(userMessage);
        aiMessage.setIntent(intent);
        aiMessage.setRiskLevel(riskLevel);
        aiMessage.setResponseType(responseType);
        aiMessage.setAiResponse(aiResponse);
        aiMessageRepository.save(aiMessage);

        // Step 7: Build and return response DTO
        AiCoachResponseDto responseDto = new AiCoachResponseDto();
        responseDto.setUserMessage(userMessage);
        responseDto.setAiResponse(aiResponse);
        responseDto.setIntent(intent);
        responseDto.setRiskLevel(riskLevel);
        responseDto.setResponseType(responseType);
        return responseDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiMessage> getRecentMessages(User user) {
        return aiMessageRepository.findByUserOrderByCreatedAtDesc(user);
    }

    // =========================================================================
    // Pipeline Step: Risk Level Classification
    // =========================================================================

    /**
     * Classifies the risk level of a user message using keyword matching.
     * HIGH risk is checked first to ensure safety-critical messages are never missed.
     */
    private RiskLevel detectRiskLevel(String message) {
        String lower = message.toLowerCase();

        // HIGH: self-harm, suicide, or immediate danger language
        if (containsAny(lower, "suicide", "kill myself", "end my life",
                "self harm", "self-harm", "hurt myself", "harm myself", "danger")) {
            return RiskLevel.HIGH;
        }

        // MEDIUM: strong distress but no immediate danger
        if (containsAny(lower, "panic", "overwhelmed", "can't handle", "cannot handle",
                "distressed", "breaking down", "can't cope", "cannot cope",
                "falling apart", "losing control")) {
            return RiskLevel.MEDIUM;
        }

        return RiskLevel.LOW;
    }

    // =========================================================================
    // Pipeline Step: Intent Detection
    // =========================================================================

    /**
     * Detects the user's coaching intent from message keywords.
     * If risk is HIGH, intent is always CRISIS_OR_SELF_HARM regardless of other keywords.
     */
    private CoachIntent detectIntent(String message, RiskLevel riskLevel) {
        if (riskLevel == RiskLevel.HIGH) {
            return CoachIntent.CRISIS_OR_SELF_HARM;
        }

        String lower = message.toLowerCase();

        // Urge / compulsion support
        if (containsAny(lower, "urge", "craving", "compulsion", "compulsive",
                "ritual", "checking")) {
            return CoachIntent.URGE_SUPPORT;
        }

        // Exposure / avoidance reflection
        if (containsAny(lower, "exposure", "task", "fear", "avoid", "avoidance")) {
            return CoachIntent.EXPOSURE_REFLECTION;
        }

        // Reassurance seeking
        if (containsAny(lower, "reassurance", "reassure", "promise",
                "guarantee", "am i okay", "is this normal")) {
            return CoachIntent.REASSURANCE_SEEKING;
        }

        // General education / progress
        if (containsAny(lower, "progress", "improving", "streak", "better",
                "success", "learn", "understand", "what is", "how does")) {
            return CoachIntent.GENERAL_EDUCATION;
        }

        return CoachIntent.OUT_OF_SCOPE;
    }

    // =========================================================================
    // Pipeline Step: Response Type Selection
    // =========================================================================

    /**
     * Maps the detected intent and risk level to a response type.
     * Safety redirects always take priority.
     */
    private ResponseType selectResponseType(CoachIntent intent, RiskLevel riskLevel) {
        if (riskLevel == RiskLevel.HIGH) {
            return ResponseType.CRISIS_STATIC_MESSAGE;
        }

        return switch (intent) {
            case URGE_SUPPORT -> ResponseType.ERP_REDIRECT;
            case EXPOSURE_REFLECTION -> ResponseType.REFLECTION_PROMPT;
            case REASSURANCE_SEEKING -> ResponseType.ERP_REDIRECT;
            case GENERAL_EDUCATION -> ResponseType.EDUCATIONAL;
            case CRISIS_OR_SELF_HARM -> ResponseType.CRISIS_STATIC_MESSAGE;
            case OUT_OF_SCOPE -> ResponseType.OUT_OF_SCOPE_MESSAGE;
        };
    }

    // =========================================================================
    // Pipeline Step: Controlled Response Generation
    // =========================================================================

    /**
     * Generates a deterministic, controlled response based on classification results.
     * No AI/LLM is involved — every response is a fixed, clinically-safe string.
     */
    private String generateResponse(CoachIntent intent, RiskLevel riskLevel, ResponseType responseType) {
        return switch (responseType) {
            case CRISIS_STATIC_MESSAGE ->
                    "I'm sorry you're feeling this way. I'm not able to handle emergencies, "
                            + "but your safety matters. Please contact local emergency services "
                            + "or reach out to someone you trust immediately.";

            case ERP_REDIRECT ->
                    intent == CoachIntent.REASSURANCE_SEEKING
                            ? "I notice you may be looking for reassurance. Instead of providing that, "
                            + "let's try a different approach: pause for 60 seconds, notice the urge "
                            + "without acting on it, and rate its intensity from 1 to 10."
                            : "Pause for 60 seconds. Notice the urge without fighting it. "
                            + "Rate its intensity from 1 to 10, breathe slowly, "
                            + "and delay the next action by five minutes.";

            case REFLECTION_PROMPT ->
                    "Choose the smallest exposure step you can complete safely. "
                            + "The goal is not perfection. The goal is repetition "
                            + "without compulsive checking.";

            case EDUCATIONAL ->
                    "Progress is measured by reduced reaction time, better delay, "
                            + "and repeated attempts. One difficult moment does not "
                            + "erase previous progress.";

            case OUT_OF_SCOPE_MESSAGE ->
                    "Let's slow this down. Describe the situation, "
                            + "the trigger, and the next small action you can take.";
        };
    }

    // =========================================================================
    // Utility
    // =========================================================================

    /**
     * Returns true if the text contains any of the given keywords.
     */
    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
