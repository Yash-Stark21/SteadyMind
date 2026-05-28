package com.stark.steadyai.controller;

import com.stark.steadyai.entity.User;
import com.stark.steadyai.service.TherapistService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
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
