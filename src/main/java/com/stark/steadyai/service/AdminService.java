package com.stark.steadyai.service;

import com.stark.steadyai.entity.TherapistAssignment;
import com.stark.steadyai.entity.User;
import com.stark.steadyai.enums.Role;
import com.stark.steadyai.repository.TherapistAssignmentRepository;
import com.stark.steadyai.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final TherapistAssignmentRepository assignmentRepository;

    public AdminService(UserRepository userRepository, TherapistAssignmentRepository assignmentRepository) {
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void assignTherapist(Long therapistId, Long userId) {
        User therapist = userRepository.findById(therapistId)
                .orElseThrow(() -> new IllegalArgumentException("Therapist not found"));
        if (therapist.getRole() != Role.ROLE_THERAPIST) {
            throw new IllegalArgumentException("Provided therapist ID does not belong to a user with ROLE_THERAPIST");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (assignmentRepository.existsByTherapistAndAssignedUser(therapist, user)) {
            throw new IllegalArgumentException("User is already assigned to this therapist");
        }

        TherapistAssignment assignment = new TherapistAssignment(therapist, user);
        assignmentRepository.save(assignment);
    }
}
