package com.stark.steadyai.repository;

import com.stark.steadyai.entity.TherapistAssignment;
import com.stark.steadyai.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TherapistAssignmentRepository extends JpaRepository<TherapistAssignment, Long> {
    List<TherapistAssignment> findByTherapist(User therapist);
    Optional<TherapistAssignment> findByAssignedUser(User assignedUser);
    boolean existsByTherapistAndAssignedUser(User therapist, User assignedUser);
}
