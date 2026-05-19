package com.stark.steadyai.controller;

import com.stark.steadyai.dto.ExposureTaskRequest;
import com.stark.steadyai.dto.ExposureTaskResponse;
import com.stark.steadyai.service.ExposureTaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exposure-tasks")
public class ExposureTaskController {

    private final ExposureTaskService exposureTaskService;

    public ExposureTaskController(ExposureTaskService exposureTaskService) {
        this.exposureTaskService = exposureTaskService;
    }

    @PostMapping
    public ResponseEntity<ExposureTaskResponse> createExposureTask(@Valid @RequestBody ExposureTaskRequest request) {
        ExposureTaskResponse response = exposureTaskService.createExposureTask(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ExposureTaskResponse>> getAllExposureTasks() {
        List<ExposureTaskResponse> response = exposureTaskService.getAllExposureTasksForCurrentUser();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExposureTaskResponse> getExposureTaskById(@PathVariable Long id) {
        ExposureTaskResponse response = exposureTaskService.getExposureTaskById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExposureTaskResponse> updateExposureTask(
            @PathVariable Long id,
            @Valid @RequestBody ExposureTaskRequest request) {
        ExposureTaskResponse response = exposureTaskService.updateExposureTask(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/start")
    public ResponseEntity<ExposureTaskResponse> markTaskInProgress(@PathVariable Long id) {
        ExposureTaskResponse response = exposureTaskService.markTaskInProgress(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<ExposureTaskResponse> markTaskCompleted(@PathVariable Long id) {
        ExposureTaskResponse response = exposureTaskService.markTaskCompleted(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/skip")
    public ResponseEntity<ExposureTaskResponse> markTaskSkipped(@PathVariable Long id) {
        ExposureTaskResponse response = exposureTaskService.markTaskSkipped(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExposureTask(@PathVariable Long id) {
        exposureTaskService.deleteExposureTask(id);
        return ResponseEntity.noContent().build();
    }
}
