package com.stark.steadyai.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * MVC controller for the AI Coach placeholder page.
 * Does NOT call any AI/LLM service directly.
 * The actual AI integration will be connected through the controlled backend pipeline.
 */
@Controller
public class AiCoachViewController {

    @GetMapping("/ai-coach")
    public String aiCoach() {
        return "ai-coach";
    }
}
