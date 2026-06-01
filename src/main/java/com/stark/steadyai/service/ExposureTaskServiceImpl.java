package com.stark.steadyai.service;

import com.stark.steadyai.dto.ExposureTaskRequest;
import com.stark.steadyai.dto.ExposureTaskResponse;
import com.stark.steadyai.entity.ExposureTask;
import com.stark.steadyai.entity.User;
import com.stark.steadyai.enums.ExposureStatus;
import com.stark.steadyai.exception.ResourceNotFoundException;
import com.stark.steadyai.repository.ExposureTaskRepository;
import com.stark.steadyai.repository.UserRepository;
import com.stark.steadyai.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ExposureTaskServiceImpl implements ExposureTaskService {

    private final ExposureTaskRepository exposureTaskRepository;
    private final UserRepository userRepository;

    public ExposureTaskServiceImpl(ExposureTaskRepository exposureTaskRepository, UserRepository userRepository) {
        this.exposureTaskRepository = exposureTaskRepository;
        this.userRepository = userRepository;
    }



    @Override
    public ExposureTaskResponse createExposureTask(ExposureTaskRequest request) {
        User user = SecurityUtils.getCurrentUser();

        ExposureTask task = new ExposureTask();
        task.setUser(user);
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setDifficultyLevel(request.difficultyLevel());
        task.setTargetDate(request.targetDate());
        
        ExposureTask savedTask = exposureTaskRepository.save(task);
        return mapToResponse(savedTask);
    }

    @Override
    public List<ExposureTaskResponse> getAllExposureTasksForCurrentUser() {
        User user = SecurityUtils.getCurrentUser();
        List<ExposureTask> tasks = exposureTaskRepository.findByUserOrderByDifficultyLevelAsc(user);
        return tasks.stream().map(this::mapToResponse).collect(Collectors.toList());
    }
    
    @Override
    public List<ExposureTaskResponse> getExposureTasksByStatus(ExposureStatus status) {
        User user = SecurityUtils.getCurrentUser();
        List<ExposureTask> tasks = exposureTaskRepository.findByUserAndStatus(user, status);
        return tasks.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public ExposureTaskResponse getExposureTaskById(Long id) {
        ExposureTask task = getTaskByIdAndUser(id);
        return mapToResponse(task);
    }

    @Override
    public ExposureTaskResponse updateExposureTask(Long id, ExposureTaskRequest request) {
        ExposureTask task = getTaskByIdAndUser(id);

        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setDifficultyLevel(request.difficultyLevel());
        task.setTargetDate(request.targetDate());

        ExposureTask updatedTask = exposureTaskRepository.save(task);
        return mapToResponse(updatedTask);
    }

    @Override
    public ExposureTaskResponse markTaskInProgress(Long id) {
        ExposureTask task = getTaskByIdAndUser(id);
        changeStatus(task, ExposureStatus.IN_PROGRESS);
        return mapToResponse(exposureTaskRepository.save(task));
    }

    @Override
    public ExposureTaskResponse markTaskCompleted(Long id) {
        ExposureTask task = getTaskByIdAndUser(id);
        changeStatus(task, ExposureStatus.COMPLETED);
        return mapToResponse(exposureTaskRepository.save(task));
    }

    @Override
    public ExposureTaskResponse markTaskSkipped(Long id) {
        ExposureTask task = getTaskByIdAndUser(id);
        changeStatus(task, ExposureStatus.SKIPPED);
        return mapToResponse(exposureTaskRepository.save(task));
    }

    @Override
    public void deleteExposureTask(Long id) {
        ExposureTask task = getTaskByIdAndUser(id);
        exposureTaskRepository.delete(task);
    }

    private ExposureTask getTaskByIdAndUser(Long id) {
        User user = SecurityUtils.getCurrentUser();
        return exposureTaskRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Exposure task not found or unauthorized access: " + id));
    }
    
    private void changeStatus(ExposureTask task, ExposureStatus newStatus) {
        task.setStatus(newStatus);
        if (newStatus == ExposureStatus.COMPLETED) {
            task.setCompletedAt(LocalDateTime.now());
        } else {
            task.setCompletedAt(null);
        }
    }

    private ExposureTaskResponse mapToResponse(ExposureTask task) {
        return new ExposureTaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getDifficultyLevel(),
                task.getStatus(),
                task.getTargetDate(),
                task.getCompletedAt(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
