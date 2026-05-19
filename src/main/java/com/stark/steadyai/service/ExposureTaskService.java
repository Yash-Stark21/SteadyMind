package com.stark.steadyai.service;

import com.stark.steadyai.dto.ExposureTaskRequest;
import com.stark.steadyai.dto.ExposureTaskResponse;
import com.stark.steadyai.enums.ExposureStatus;

import java.util.List;

public interface ExposureTaskService {
    ExposureTaskResponse createExposureTask(ExposureTaskRequest request);
    List<ExposureTaskResponse> getAllExposureTasksForCurrentUser();
    List<ExposureTaskResponse> getExposureTasksByStatus(ExposureStatus status);
    ExposureTaskResponse getExposureTaskById(Long id);
    ExposureTaskResponse updateExposureTask(Long id, ExposureTaskRequest request);
    ExposureTaskResponse markTaskInProgress(Long id);
    ExposureTaskResponse markTaskCompleted(Long id);
    ExposureTaskResponse markTaskSkipped(Long id);
    void deleteExposureTask(Long id);
}
