package com.stark.steadyai.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * MVC controller for the Compulsion Delay Attempts page.
 * Serves the Thymeleaf shell; data is loaded client-side via JavaScript fetch()
 * from the REST API in CompulsionDelayAttemptController.
 */
@Controller
public class DelayAttemptViewController {

    @GetMapping("/delay-attempts")
    public String viewDelayAttempts() {
        return "delay-attempts";
    }
}
