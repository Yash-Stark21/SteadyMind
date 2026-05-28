package com.stark.steadyai.service;

import com.stark.steadyai.dto.CompleteDelayAttemptRequest;
import com.stark.steadyai.dto.CompulsionDelayAttemptRequest;
import com.stark.steadyai.dto.CompulsionDelayAttemptResponse;
import com.stark.steadyai.dto.CompulsionDelayAttemptUpdateRequest;
import com.stark.steadyai.entity.CompulsionDelayAttempt;
import com.stark.steadyai.entity.ExposureTask;
import com.stark.steadyai.entity.UrgeLog;
import com.stark.steadyai.entity.User;
import com.stark.steadyai.enums.CompulsionDelayOutcome;
import com.stark.steadyai.exception.InvalidStateException;
import com.stark.steadyai.exception.ResourceNotFoundException;
import com.stark.steadyai.repository.CompulsionDelayAttemptRepository;
import com.stark.steadyai.repository.ExposureTaskRepository;
import com.stark.steadyai.repository.UrgeLogRepository;
import com.stark.steadyai.repository.UserRepository;
import com.stark.steadyai.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CompulsionDelayAttemptServiceImpl implements CompulsionDelayAttemptService {

    private final CompulsionDelayAttemptRepository delayAttemptRepository;
    private final UrgeLogRepository urgeLogRepository;
    private final ExposureTaskRepository exposureTaskRepository;
    private final UserRepository userRepository;

    public CompulsionDelayAttemptServiceImpl(CompulsionDelayAttemptRepository delayAttemptRepository,
                                             UrgeLogRepository urgeLogRepository,
                                             ExposureTaskRepository exposureTaskRepository,
                                             UserRepository userRepository) {
        this.delayAttemptRepository = delayAttemptRepository;
        this.urgeLogRepository = urgeLogRepository;
        this.exposureTaskRepository = exposureTaskRepository;
        this.userRepository = userRepository;
    }


    @Override
    public CompulsionDelayAttemptResponse createDelayAttempt(CompulsionDelayAttemptRequest request) {
        User user = SecurityUtils.getCurrentUser();

        CompulsionDelayAttempt attempt = new CompulsionDelayAttempt();
        attempt.setUser(user);
        attempt.setTriggerDescription(request.getTriggerDescription());
        attempt.setPlannedDelayMinutes(request.getPlannedDelayMinutes());
        attempt.setCopingStrategyUsed(request.getCopingStrategyUsed());
        attempt.setNotes(request.getNotes());
        attempt.setStartedAt(LocalDateTime.now());

        if (request.getUrgeLogId() != null) {
            UrgeLog urgeLog = urgeLogRepository.findByIdAndUser(request.getUrgeLogId(), user)
                    .orElseThrow(() -> new ResourceNotFoundException("Urge log not found or unauthorized: " + request.getUrgeLogId()));
            attempt.setUrgeLog(urgeLog);
        }

        if (request.getExposureTaskId() != null) {
            ExposureTask exposureTask = exposureTaskRepository.findByIdAndUser(request.getExposureTaskId(), user)
                    .orElseThrow(() -> new ResourceNotFoundException("Exposure task not found or unauthorized: " + request.getExposureTaskId()));
            attempt.setExposureTask(exposureTask);
        }

        CompulsionDelayAttempt savedAttempt = delayAttemptRepository.save(attempt);
        return mapToResponse(savedAttempt);
    }

    @Override
    public List<CompulsionDelayAttemptResponse> getAllDelayAttemptsForCurrentUser() {
        User user = SecurityUtils.getCurrentUser();
        List<CompulsionDelayAttempt> attempts = delayAttemptRepository.findByUserOrderByCreatedAtDesc(user);
        return attempts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CompulsionDelayAttemptResponse getDelayAttemptById(Long id) {
        CompulsionDelayAttempt attempt = getAttemptByIdAndUser(id);
        return mapToResponse(attempt);
    }

    @Override
    public CompulsionDelayAttemptResponse updateDelayAttempt(Long id, CompulsionDelayAttemptUpdateRequest request) {
        CompulsionDelayAttempt attempt = getAttemptByIdAndUser(id);

        attempt.setTriggerDescription(request.getTriggerDescription());
        attempt.setPlannedDelayMinutes(request.getPlannedDelayMinutes());
        attempt.setCopingStrategyUsed(request.getCopingStrategyUsed());
        attempt.setNotes(request.getNotes());

        CompulsionDelayAttempt updatedAttempt = delayAttemptRepository.save(attempt);
        return mapToResponse(updatedAttempt);
    }

    @Override
    public CompulsionDelayAttemptResponse completeDelayAttempt(Long id, CompleteDelayAttemptRequest request) {
        CompulsionDelayAttempt attempt = getAttemptByIdAndUser(id);

        if (attempt.getOutcome() != null) {
            throw new InvalidStateException("Cannot complete an attempt that is already completed or cancelled.");
        }

        attempt.setActualDelayMinutes(request.getActualDelayMinutes());
        attempt.setOutcome(request.getOutcome());
        attempt.setEndedAt(LocalDateTime.now());

        CompulsionDelayAttempt updatedAttempt = delayAttemptRepository.save(attempt);
        return mapToResponse(updatedAttempt);
    }

    @Override
    public CompulsionDelayAttemptResponse cancelDelayAttempt(Long id) {
        CompulsionDelayAttempt attempt = getAttemptByIdAndUser(id);

        if (attempt.getOutcome() != null) {
            throw new InvalidStateException("Cannot cancel an attempt that is already completed or cancelled.");
        }

        attempt.setOutcome(CompulsionDelayOutcome.CANCELLED);
        attempt.setEndedAt(LocalDateTime.now());

        CompulsionDelayAttempt updatedAttempt = delayAttemptRepository.save(attempt);
        return mapToResponse(updatedAttempt);
    }

    @Override
    public void deleteDelayAttempt(Long id) {
        CompulsionDelayAttempt attempt = getAttemptByIdAndUser(id);
        delayAttemptRepository.delete(attempt);
    }

    private CompulsionDelayAttempt getAttemptByIdAndUser(Long id) {
        User user = SecurityUtils.getCurrentUser();
        return delayAttemptRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Compulsion delay attempt not found or unauthorized access: " + id));
    }

    private CompulsionDelayAttemptResponse mapToResponse(CompulsionDelayAttempt attempt) {
        CompulsionDelayAttemptResponse response = new CompulsionDelayAttemptResponse();
        response.setId(attempt.getId());
        
        if (attempt.getUrgeLog() != null) {
            response.setUrgeLogId(attempt.getUrgeLog().getId());
        }
        
        if (attempt.getExposureTask() != null) {
            response.setExposureTaskId(attempt.getExposureTask().getId());
        }

        response.setTriggerDescription(attempt.getTriggerDescription());
        response.setPlannedDelayMinutes(attempt.getPlannedDelayMinutes());
        response.setActualDelayMinutes(attempt.getActualDelayMinutes());
        response.setOutcome(attempt.getOutcome());
        response.setCopingStrategyUsed(attempt.getCopingStrategyUsed());
        response.setNotes(attempt.getNotes());
        response.setStartedAt(attempt.getStartedAt());
        response.setEndedAt(attempt.getEndedAt());
        response.setCreatedAt(attempt.getCreatedAt());
        response.setUpdatedAt(attempt.getUpdatedAt());

        return response;
    }
}
