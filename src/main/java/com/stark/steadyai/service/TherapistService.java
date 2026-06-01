package com.stark.steadyai.service;

import com.stark.steadyai.dto.AiCoachExchangeResponseDto;
import com.stark.steadyai.dto.AiConversationSummaryDto;
import com.stark.steadyai.dto.CompulsionDelayAttemptResponse;
import com.stark.steadyai.dto.ExposureTaskResponse;
import com.stark.steadyai.dto.TherapistPatientSummaryDto;
import com.stark.steadyai.dto.UrgeLogResponse;
import com.stark.steadyai.entity.AiConversation;
import com.stark.steadyai.entity.AiMessage;
import com.stark.steadyai.entity.CompulsionDelayAttempt;
import com.stark.steadyai.entity.ExposureTask;
import com.stark.steadyai.entity.TherapistAssignment;
import com.stark.steadyai.entity.UrgeLog;
import com.stark.steadyai.entity.User;
import com.stark.steadyai.enums.ExposureStatus;
import com.stark.steadyai.exception.ResourceNotFoundException;
import com.stark.steadyai.repository.AiConversationRepository;
import com.stark.steadyai.repository.AiMessageRepository;
import com.stark.steadyai.repository.CompulsionDelayAttemptRepository;
import com.stark.steadyai.repository.ExposureTaskRepository;
import com.stark.steadyai.repository.TherapistAssignmentRepository;
import com.stark.steadyai.repository.UrgeLogRepository;
import com.stark.steadyai.repository.UserRepository;
import com.stark.steadyai.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class TherapistService {

    private final TherapistAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final UrgeLogRepository urgeLogRepository;
    private final ExposureTaskRepository exposureTaskRepository;
    private final CompulsionDelayAttemptRepository delayAttemptRepository;
    private final AiConversationRepository aiConversationRepository;
    private final AiMessageRepository aiMessageRepository;

    public TherapistService(TherapistAssignmentRepository assignmentRepository,
                            UserRepository userRepository,
                            UrgeLogRepository urgeLogRepository,
                            ExposureTaskRepository exposureTaskRepository,
                            CompulsionDelayAttemptRepository delayAttemptRepository,
                            AiConversationRepository aiConversationRepository,
                            AiMessageRepository aiMessageRepository) {
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
        this.urgeLogRepository = urgeLogRepository;
        this.exposureTaskRepository = exposureTaskRepository;
        this.delayAttemptRepository = delayAttemptRepository;
        this.aiConversationRepository = aiConversationRepository;
        this.aiMessageRepository = aiMessageRepository;
    }

    public List<User> getAssignedPatients() {
        return getAssignedPatients(SecurityUtils.getCurrentUser());
    }

    public List<User> getAssignedPatients(User therapist) {
        return assignmentRepository.findByTherapist(therapist).stream()
                .map(TherapistAssignment::getAssignedUser)
                .toList();
    }

    public List<TherapistPatientSummaryDto> getAssignedPatientSummaries(User therapist) {
        return getAssignedPatients(therapist).stream()
                .map(this::buildPatientSummary)
                .toList();
    }

    public User requireAssignedPatient(Long patientId) {
        return requireAssignedPatient(patientId, SecurityUtils.getCurrentUser());
    }

    public User requireAssignedPatient(Long patientId, User therapist) {
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Assigned patient not found."));

        if (!assignmentRepository.existsByTherapistAndAssignedUser(therapist, patient)) {
            throw new ResourceNotFoundException("Assigned patient not found.");
        }

        return patient;
    }

    public TherapistPatientSummaryDto getPatientSummary(User patient) {
        return buildPatientSummary(patient);
    }

    public List<UrgeLogResponse> getPatientUrgeLogs(User patient) {
        return urgeLogRepository.findByUserOrderByCreatedAtDesc(patient).stream()
                .map(this::mapUrgeLog)
                .toList();
    }

    public List<ExposureTaskResponse> getPatientExposureTasks(User patient) {
        return exposureTaskRepository.findByUserOrderByDifficultyLevelAsc(patient).stream()
                .map(this::mapExposureTask)
                .toList();
    }

    public List<CompulsionDelayAttemptResponse> getPatientDelayAttempts(User patient) {
        return delayAttemptRepository.findByUserOrderByCreatedAtDesc(patient).stream()
                .map(this::mapDelayAttempt)
                .toList();
    }

    public List<AiConversationSummaryDto> getPatientAiConversations(User patient) {
        return aiConversationRepository.findByUserOrderByUpdatedAtDesc(patient).stream()
                .map(this::mapConversation)
                .toList();
    }

    public List<AiCoachExchangeResponseDto> getPatientConversationMessages(Long patientId, Long conversationId) {
        return getPatientConversationMessages(patientId, conversationId, SecurityUtils.getCurrentUser());
    }

    public List<AiCoachExchangeResponseDto> getPatientConversationMessages(Long patientId,
                                                                           Long conversationId,
                                                                           User therapist) {
        User patient = requireAssignedPatient(patientId, therapist);
        AiConversation conversation = aiConversationRepository.findByIdAndUser(conversationId, patient)
                .orElseThrow(() -> new ResourceNotFoundException("AI conversation not found."));

        return aiMessageRepository.findByConversationOrderByCreatedAtAsc(conversation).stream()
                .map(this::mapAiMessage)
                .toList();
    }

    public boolean isPatientAssignedToTherapist(Long patientId, User therapist) {
        return userRepository.findById(patientId)
                .map(patient -> assignmentRepository.existsByTherapistAndAssignedUser(therapist, patient))
                .orElse(false);
    }

    private TherapistPatientSummaryDto buildPatientSummary(User patient) {
        List<UrgeLog> urgeLogs = urgeLogRepository.findByUserOrderByCreatedAtDesc(patient);
        List<ExposureTask> exposureTasks = exposureTaskRepository.findByUserOrderByDifficultyLevelAsc(patient);
        List<CompulsionDelayAttempt> delayAttempts = delayAttemptRepository.findByUserOrderByCreatedAtDesc(patient);
        List<AiConversation> conversations = aiConversationRepository.findByUserOrderByUpdatedAtDesc(patient);

        long completedExposureTasks = exposureTasks.stream()
                .filter(task -> task.getStatus() == ExposureStatus.COMPLETED)
                .count();

        return new TherapistPatientSummaryDto(
                patient.getId(),
                patient.getName(),
                patient.getEmail(),
                urgeLogs.size(),
                exposureTasks.size(),
                completedExposureTasks,
                delayAttempts.size(),
                conversations.size()
        );
    }

    private UrgeLogResponse mapUrgeLog(UrgeLog log) {
        return new UrgeLogResponse(
                log.getId(),
                log.getTriggerText(),
                log.getObsessionText(),
                log.getCompulsionUrge(),
                log.getIntensityBefore(),
                log.getDelayMinutes(),
                log.getIntensityAfter(),
                log.getCompulsionPerformed(),
                log.getCreatedAt()
        );
    }

    private ExposureTaskResponse mapExposureTask(ExposureTask task) {
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

    private CompulsionDelayAttemptResponse mapDelayAttempt(CompulsionDelayAttempt attempt) {
        Long urgeLogId = attempt.getUrgeLog() != null ? attempt.getUrgeLog().getId() : null;
        Long exposureTaskId = attempt.getExposureTask() != null ? attempt.getExposureTask().getId() : null;

        return new CompulsionDelayAttemptResponse(
                attempt.getId(),
                urgeLogId,
                exposureTaskId,
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

    private AiConversationSummaryDto mapConversation(AiConversation conversation) {
        return new AiConversationSummaryDto(
                conversation.getId(),
                conversation.getFirstQuestion(),
                conversation.getEndedAt()
        );
    }

    private AiCoachExchangeResponseDto mapAiMessage(AiMessage message) {
        return new AiCoachExchangeResponseDto(
                message.getConversation().getId(),
                message.getUserMessage(),
                message.getAiResponse(),
                message.getIntent(),
                message.getRiskLevel(),
                message.getResponseType(),
                message.getCreatedAt()
        );
    }
}
