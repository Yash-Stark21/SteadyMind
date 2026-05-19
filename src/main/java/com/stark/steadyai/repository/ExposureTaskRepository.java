package com.stark.steadyai.repository;

import com.stark.steadyai.entity.ExposureTask;
import com.stark.steadyai.entity.User;
import com.stark.steadyai.enums.ExposureStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExposureTaskRepository extends JpaRepository<ExposureTask, Long> {
    List<ExposureTask> findByUserOrderByDifficultyLevelAsc(User user);
    List<ExposureTask> findByUserAndStatus(User user, ExposureStatus status);
    Optional<ExposureTask> findByIdAndUser(Long id, User user);
}
