package com.stark.steadyai.controller;

import com.stark.steadyai.dto.CompleteDelayAttemptRequest;
import com.stark.steadyai.dto.CompulsionDelayAttemptRequest;
import com.stark.steadyai.dto.CompulsionDelayAttemptResponse;
import com.stark.steadyai.dto.CompulsionDelayAttemptUpdateRequest;
import com.stark.steadyai.service.CompulsionDelayAttemptService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/compulsion-delay-attempts")
public class CompulsionDelayAttemptController {

    private final CompulsionDelayAttemptService delayAttemptService;

    public CompulsionDelayAttemptController(CompulsionDelayAttemptService delayAttemptService) {
        this.delayAttemptService = delayAttemptService;
    }

    @PostMapping
    public ResponseEntity<CompulsionDelayAttemptResponse> createDelayAttempt(@Valid @RequestBody CompulsionDelayAttemptRequest request) {
        CompulsionDelayAttemptResponse response = delayAttemptService.createDelayAttempt(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CompulsionDelayAttemptResponse>> getAllDelayAttempts() {
        List<CompulsionDelayAttemptResponse> responses = delayAttemptService.getAllDelayAttemptsForCurrentUser();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompulsionDelayAttemptResponse> getDelayAttemptById(@PathVariable Long id) {
        CompulsionDelayAttemptResponse response = delayAttemptService.getDelayAttemptById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompulsionDelayAttemptResponse> updateDelayAttempt(
            @PathVariable Long id,
            @Valid @RequestBody CompulsionDelayAttemptUpdateRequest request) {
        CompulsionDelayAttemptResponse response = delayAttemptService.updateDelayAttempt(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<CompulsionDelayAttemptResponse> completeDelayAttempt(
            @PathVariable Long id,
            @Valid @RequestBody CompleteDelayAttemptRequest request) {
        CompulsionDelayAttemptResponse response = delayAttemptService.completeDelayAttempt(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<CompulsionDelayAttemptResponse> cancelDelayAttempt(@PathVariable Long id) {
        CompulsionDelayAttemptResponse response = delayAttemptService.cancelDelayAttempt(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDelayAttempt(@PathVariable Long id) {
        delayAttemptService.deleteDelayAttempt(id);
        return ResponseEntity.noContent().build();
    }
}
