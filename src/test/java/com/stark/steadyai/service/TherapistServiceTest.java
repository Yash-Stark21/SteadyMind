package com.stark.steadyai.service;

import com.stark.steadyai.dto.AiCoachExchangeResponseDto;
import com.stark.steadyai.dto.TherapistPatientSummaryDto;
import com.stark.steadyai.entity.AiConversation;
import com.stark.steadyai.entity.AiMessage;
import com.stark.steadyai.entity.CompulsionDelayAttempt;
import com.stark.steadyai.entity.ExposureTask;
import com.stark.steadyai.entity.TherapistAssignment;
import com.stark.steadyai.entity.UrgeLog;
import com.stark.steadyai.entity.User;
import com.stark.steadyai.enums.CoachIntent;
import com.stark.steadyai.enums.ExposureDifficulty;
import com.stark.steadyai.enums.ExposureStatus;
import com.stark.steadyai.enums.ResponseType;
import com.stark.steadyai.enums.RiskLevel;
import com.stark.steadyai.exception.ResourceNotFoundException;
import com.stark.steadyai.repository.AiConversationRepository;
import com.stark.steadyai.repository.AiMessageRepository;
import com.stark.steadyai.repository.CompulsionDelayAttemptRepository;
import com.stark.steadyai.repository.ExposureTaskRepository;
import com.stark.steadyai.repository.TherapistAssignmentRepository;
import com.stark.steadyai.repository.UrgeLogRepository;
import com.stark.steadyai.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TherapistServiceTest {

    @Mock
    private TherapistAssignmentRepository assignmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UrgeLogRepository urgeLogRepository;

    @Mock
    private ExposureTaskRepository exposureTaskRepository;

    @Mock
    private CompulsionDelayAttemptRepository delayAttemptRepository;

    @Mock
    private AiConversationRepository aiConversationRepository;

    @Mock
    private AiMessageRepository aiMessageRepository;

    private TherapistService therapistService;
    private User therapist;
    private User patient;
    private User unassignedPatient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        therapistService = new TherapistService(
                assignmentRepository,
                userRepository,
                urgeLogRepository,
                exposureTaskRepository,
                delayAttemptRepository,
                aiConversationRepository,
                aiMessageRepository
        );

        therapist = user(1L, "Therapist", "therapist@example.com");
        patient = user(2L, "Patient", "patient@example.com");
        unassignedPatient = user(3L, "Other Patient", "other@example.com");
    }

    @Test
    void assignedPatientsAreListed() {
        when(assignmentRepository.findByTherapist(therapist))
                .thenReturn(List.of(new TherapistAssignment(therapist, patient)));

        List<User> patients = therapistService.getAssignedPatients(therapist);

        assertThat(patients).containsExactly(patient);
    }

    @Test
    void assignedPatientDetailAccessSucceeds() {
        when(userRepository.findById(patient.getId())).thenReturn(Optional.of(patient));
        when(assignmentRepository.existsByTherapistAndAssignedUser(therapist, patient)).thenReturn(true);

        User result = therapistService.requireAssignedPatient(patient.getId(), therapist);

        assertThat(result).isSameAs(patient);
    }

    @Test
    void unassignedPatientAccessIsRejected() {
        when(userRepository.findById(unassignedPatient.getId())).thenReturn(Optional.of(unassignedPatient));
        when(assignmentRepository.existsByTherapistAndAssignedUser(therapist, unassignedPatient)).thenReturn(false);

        assertThatThrownBy(() -> therapistService.requireAssignedPatient(unassignedPatient.getId(), therapist))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Assigned patient not found.");
    }

    @Test
    void patientSummaryUsesOnlyRequestedPatientData() {
        UrgeLog urgeLog = new UrgeLog();
        ExposureTask completedTask = exposureTask(ExposureStatus.COMPLETED);
        ExposureTask pendingTask = exposureTask(ExposureStatus.PENDING);
        CompulsionDelayAttempt delayAttempt = new CompulsionDelayAttempt();
        AiConversation conversation = new AiConversation();

        when(urgeLogRepository.findByUserOrderByCreatedAtDesc(patient)).thenReturn(List.of(urgeLog));
        when(exposureTaskRepository.findByUserOrderByDifficultyLevelAsc(patient)).thenReturn(List.of(completedTask, pendingTask));
        when(delayAttemptRepository.findByUserOrderByCreatedAtDesc(patient)).thenReturn(List.of(delayAttempt));
        when(aiConversationRepository.findByUserOrderByUpdatedAtDesc(patient)).thenReturn(List.of(conversation));

        TherapistPatientSummaryDto summary = therapistService.getPatientSummary(patient);

        assertThat(summary.patientId()).isEqualTo(patient.getId());
        assertThat(summary.totalUrgeLogs()).isEqualTo(1);
        assertThat(summary.totalExposureTasks()).isEqualTo(2);
        assertThat(summary.completedExposureTasks()).isEqualTo(1);
        assertThat(summary.totalDelayAttempts()).isEqualTo(1);
        assertThat(summary.totalAiConversations()).isEqualTo(1);
        verify(urgeLogRepository).findByUserOrderByCreatedAtDesc(patient);
        verify(exposureTaskRepository).findByUserOrderByDifficultyLevelAsc(patient);
        verify(delayAttemptRepository).findByUserOrderByCreatedAtDesc(patient);
        verify(aiConversationRepository).findByUserOrderByUpdatedAtDesc(patient);
    }

    @Test
    void aiConversationMessagesRequireAssignmentAndConversationOwnership() {
        AiConversation conversation = conversation(10L, patient);
        AiMessage message = message(conversation);

        when(userRepository.findById(patient.getId())).thenReturn(Optional.of(patient));
        when(assignmentRepository.existsByTherapistAndAssignedUser(therapist, patient)).thenReturn(true);
        when(aiConversationRepository.findByIdAndUser(conversation.getId(), patient)).thenReturn(Optional.of(conversation));
        when(aiMessageRepository.findByConversationOrderByCreatedAtAsc(conversation)).thenReturn(List.of(message));

        List<AiCoachExchangeResponseDto> messages = therapistService.getPatientConversationMessages(
                patient.getId(),
                conversation.getId(),
                therapist
        );

        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).conversationId()).isEqualTo(conversation.getId());
        assertThat(messages.get(0).intent()).isEqualTo(CoachIntent.URGE_SUPPORT);
        assertThat(messages.get(0).riskLevel()).isEqualTo(RiskLevel.LOW);
        assertThat(messages.get(0).responseType()).isEqualTo(ResponseType.REFLECTION_PROMPT);
    }

    @Test
    void aiConversationMessagesRejectUnassignedPatient() {
        when(userRepository.findById(unassignedPatient.getId())).thenReturn(Optional.of(unassignedPatient));
        when(assignmentRepository.existsByTherapistAndAssignedUser(therapist, unassignedPatient)).thenReturn(false);

        assertThatThrownBy(() -> therapistService.getPatientConversationMessages(unassignedPatient.getId(), 10L, therapist))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Assigned patient not found.");
    }

    @Test
    void aiConversationMessagesRejectConversationOwnedBySomeoneElse() {
        when(userRepository.findById(patient.getId())).thenReturn(Optional.of(patient));
        when(assignmentRepository.existsByTherapistAndAssignedUser(therapist, patient)).thenReturn(true);
        when(aiConversationRepository.findByIdAndUser(10L, patient)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> therapistService.getPatientConversationMessages(patient.getId(), 10L, therapist))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("AI conversation not found.");
    }

    private User user(Long id, String name, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash("password");
        return user;
    }

    private ExposureTask exposureTask(ExposureStatus status) {
        ExposureTask task = new ExposureTask();
        task.setStatus(status);
        task.setDifficultyLevel(ExposureDifficulty.LOW);
        task.setTitle("Task");
        task.setUser(patient);
        return task;
    }

    private AiConversation conversation(Long id, User user) {
        AiConversation conversation = new AiConversation();
        conversation.setId(id);
        conversation.setUser(user);
        conversation.setFirstQuestion("First question");
        return conversation;
    }

    private AiMessage message(AiConversation conversation) {
        AiMessage message = new AiMessage();
        message.setConversation(conversation);
        message.setUser(patient);
        message.setUserMessage("I have an urge");
        message.setAiResponse("Pause and notice the urge.");
        message.setIntent(CoachIntent.URGE_SUPPORT);
        message.setRiskLevel(RiskLevel.LOW);
        message.setResponseType(ResponseType.REFLECTION_PROMPT);
        return message;
    }
}
