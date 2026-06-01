package com.stark.steadyai.controller;

import com.stark.steadyai.dto.AiCoachExchangeResponseDto;
import com.stark.steadyai.entity.User;
import com.stark.steadyai.service.TherapistService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/therapist")
@PreAuthorize("hasAuthority('ROLE_THERAPIST')")
public class TherapistController {

    private final TherapistService therapistService;

    public TherapistController(TherapistService therapistService) {
        this.therapistService = therapistService;
    }

    @GetMapping("/patients")
    public ResponseEntity<List<PatientDto>> getAssignedPatients() {
        List<PatientDto> patients = therapistService.getAssignedPatients().stream()
                .map(u -> new PatientDto(u.getId(), u.getName(), u.getEmail()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/patients/{patientId}/ai/conversations/{conversationId}/messages")
    public ResponseEntity<List<AiCoachExchangeResponseDto>> getPatientConversationMessages(
            @PathVariable Long patientId,
            @PathVariable Long conversationId) {

        List<AiCoachExchangeResponseDto> messages = therapistService.getPatientConversationMessages(patientId, conversationId);
        return ResponseEntity.ok(messages);
    }

    public static class PatientDto {
        public Long id;
        public String name;
        public String email;

        public PatientDto(Long id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }
    }
}
