package com.stark.steadyai.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * MVC controller that serves the progress analytics Thymeleaf page.
 * Data is loaded client-side via JavaScript fetch from the REST API.
 */
@Controller
public class ProgressViewController {

    @GetMapping("/progress")
    public String progress() {
        return "progress";
    }
}
