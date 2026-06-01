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
        attempt.setTriggerDescription(request.triggerDescription());
        attempt.setPlannedDelayMinutes(request.plannedDelayMinutes());
        attempt.setCopingStrategyUsed(request.copingStrategyUsed());
        attempt.setNotes(request.notes());
        attempt.setStartedAt(LocalDateTime.now());

        if (request.urgeLogId() != null) {
            UrgeLog urgeLog = urgeLogRepository.findByIdAndUser(request.urgeLogId(), user)
                    .orElseThrow(() -> new ResourceNotFoundException("Urge log not found or unauthorized: " + request.urgeLogId()));
            attempt.setUrgeLog(urgeLog);
        }

        if (request.exposureTaskId() != null) {
            ExposureTask exposureTask = exposureTaskRepository.findByIdAndUser(request.exposureTaskId(), user)
                    .orElseThrow(() -> new ResourceNotFoundException("Exposure task not found or unauthorized: " + request.exposureTaskId()));
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

        attempt.setTriggerDescription(request.triggerDescription());
        attempt.setPlannedDelayMinutes(request.plannedDelayMinutes());
        attempt.setCopingStrategyUsed(request.copingStrategyUsed());
        attempt.setNotes(request.notes());

        CompulsionDelayAttempt updatedAttempt = delayAttemptRepository.save(attempt);
        return mapToResponse(updatedAttempt);
    }

    @Override
    public CompulsionDelayAttemptResponse completeDelayAttempt(Long id, CompleteDelayAttemptRequest request) {
        CompulsionDelayAttempt attempt = getAttemptByIdAndUser(id);

        if (attempt.getOutcome() != null) {
            throw new InvalidStateException("Cannot complete an attempt that is already completed or cancelled.");
        }

        attempt.setActualDelayMinutes(request.actualDelayMinutes());
        attempt.setOutcome(request.outcome());
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
        return new CompulsionDelayAttemptResponse(
                attempt.getId(),
                attempt.getUrgeLog() != null ? attempt.getUrgeLog().getId() : null,
                attempt.getExposureTask() != null ? attempt.getExposureTask().getId() : null,
                attempt.getTriggerDescription(),
                attempt.getPlannedDelayMinutes(),
                attempt.getActualDelayMinutes(),
                attempt.getOutcome(),
                attempt.getCopingStrategyUsed(),
                attempt.getNotes(),
                attempt.getStartedAt(),
                attempt.getEndedAt(),
                attempt.getCreatedAt(),
                attempt.getUpdatedAt()
        );
    }
}
