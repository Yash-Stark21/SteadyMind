package com.stark.steadyai.controller;

import com.stark.steadyai.dto.UrgeLogRequest;
import com.stark.steadyai.dto.UrgeLogResponse;
import com.stark.steadyai.service.UrgeLogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/urge-logs")
public class UrgeLogController {

    private final UrgeLogService urgeLogService;

    public UrgeLogController(UrgeLogService urgeLogService) {
        this.urgeLogService = urgeLogService;
    }

    @PostMapping
    public ResponseEntity<UrgeLogResponse> createUrgeLog(@Valid @RequestBody UrgeLogRequest request) {
        UrgeLogResponse response = urgeLogService.createUrgeLog(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<UrgeLogResponse>> getAllUrgeLogs() {
        List<UrgeLogResponse> response = urgeLogService.getAllUrgeLogs();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UrgeLogResponse> getUrgeLogById(@PathVariable Long id) {
        UrgeLogResponse response = urgeLogService.getUrgeLogById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UrgeLogResponse> updateUrgeLog(
            @PathVariable Long id,
            @Valid @RequestBody UrgeLogRequest request) {
        UrgeLogResponse response = urgeLogService.updateUrgeLog(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUrgeLog(@PathVariable Long id) {
        urgeLogService.deleteUrgeLog(id);
        return ResponseEntity.noContent().build();
    }
}
