package com.stark.steadyai.repository;

import com.stark.steadyai.entity.CompulsionDelayAttempt;
import com.stark.steadyai.entity.User;
import com.stark.steadyai.enums.CompulsionDelayOutcome;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompulsionDelayAttemptRepository extends JpaRepository<CompulsionDelayAttempt, Long> {
    
    List<CompulsionDelayAttempt> findByUser(User user);
    
    List<CompulsionDelayAttempt> findByUserOrderByCreatedAtDesc(User user);
    
    List<CompulsionDelayAttempt> findByUserAndOutcome(User user, CompulsionDelayOutcome outcome);
    
    List<CompulsionDelayAttempt> findByUserAndUrgeLogId(User user, Long urgeLogId);
    
    List<CompulsionDelayAttempt> findByUserAndExposureTaskId(User user, Long exposureTaskId);
    
    Optional<CompulsionDelayAttempt> findByIdAndUser(Long id, User user);
}
