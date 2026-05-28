package com.stark.steadyai.service;

import com.stark.steadyai.entity.TherapistAssignment;
import com.stark.steadyai.entity.User;
import com.stark.steadyai.repository.TherapistAssignmentRepository;
import com.stark.steadyai.security.SecurityUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TherapistService {

    private final TherapistAssignmentRepository assignmentRepository;

    public TherapistService(TherapistAssignmentRepository assignmentRepository) {
        this.assignmentRepository = assignmentRepository;
    }

    public List<User> getAssignedPatients() {
        User therapist = SecurityUtils.getCurrentUser();
        return assignmentRepository.findByTherapist(therapist).stream()
                .map(TherapistAssignment::getAssignedUser)
                .collect(Collectors.toList());
    }

    public boolean isPatientAssignedToTherapist(Long patientId, User therapist) {
        return assignmentRepository.findByTherapist(therapist).stream()
                .anyMatch(assignment -> assignment.getAssignedUser().getId().equals(patientId));
    }
}
