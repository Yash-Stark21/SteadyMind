package com.stark.steadyai.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stark.steadyai.dto.AiCoachRequestDto;
import com.stark.steadyai.dto.AiCoachResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("openai")
public class SpringAiClient implements AiClient {

    private static final Logger log = LoggerFactory.getLogger(SpringAiClient.class);
    
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public SpringAiClient(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    @Override
    public AiCoachResponseDto generateResponse(AiCoachRequestDto requestDto) {
        try {
            String rawResponse = chatClient.prompt()
                    .system(buildSystemPrompt())
                    .user(requestDto.getMessage())
                    .call()
                    .content();

            String jsonString = extractJson(rawResponse);
            return objectMapper.readValue(jsonString, AiCoachResponseDto.class);
            
        } catch (Exception e) {
            log.error("Failed to generate or parse AI response: ", e);
            return null; // Return null to indicate failure, validator will handle
        }
    }

    private String extractJson(String rawResponse) {
        if (rawResponse == null) {
            return "";
        }
        String trimmed = rawResponse.trim();
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        return trimmed.trim();
    }

    private String buildSystemPrompt() {
        return """
You are not a therapist, doctor, or emergency service.
You are a structured classification assistant inside a Spring Boot backend.
Return ONLY valid JSON.
Do not include markdown.
Do not include explanations outside JSON.
Do not diagnose.
Do not promise safety.
Do not give direct reassurance.
Do not encourage checking, compulsions, reassurance seeking, confession, rumination, or repeated review.

Return JSON with exactly these fields:

{
  "intent": "URGE_SUPPORT | REASSURANCE_SEEKING | EXPOSURE_REFLECTION | GENERAL_EDUCATION | CRISIS_OR_SELF_HARM | OUT_OF_SCOPE",
  "riskLevel": "LOW | MEDIUM | HIGH",
  "responseType": "ERP_REDIRECT | REFLECTION_PROMPT | EDUCATIONAL | CRISIS_STATIC_MESSAGE | OUT_OF_SCOPE_MESSAGE",
  "directAnswerAllowed": true or false,
  "userFacingMessage": "short non-diagnostic supportive message",
  "suggestedAction": "START_DELAY_TIMER | LOG_URGE | VIEW_EXPOSURE_LIST | CONTACT_SUPPORT | NONE"
}

Rules:

If user asks for certainty, promise, confirmation, contamination reassurance, harm reassurance, or asks whether to check:
- intent = REASSURANCE_SEEKING
- responseType = ERP_REDIRECT
- directAnswerAllowed = false
- suggestedAction = START_DELAY_TIMER

If user expresses self-harm or immediate danger:
- intent = CRISIS_OR_SELF_HARM
- riskLevel = HIGH
- responseType = CRISIS_STATIC_MESSAGE
- directAnswerAllowed = false
- suggestedAction = CONTACT_SUPPORT

If user asks about urge, craving, ritual, checking, or compulsion:
- intent = URGE_SUPPORT
- responseType = REFLECTION_PROMPT
- suggestedAction = START_DELAY_TIMER

If user discusses exposure task or fear practice:
- intent = EXPOSURE_REFLECTION
- responseType = REFLECTION_PROMPT
- suggestedAction = VIEW_EXPOSURE_LIST

For general learning:
- intent = GENERAL_EDUCATION
- responseType = EDUCATIONAL
- suggestedAction = NONE

Keep userFacingMessage short, practical, non-diagnostic, and safe.
""";
    }
}
