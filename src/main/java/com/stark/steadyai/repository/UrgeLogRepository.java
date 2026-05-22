package com.stark.steadyai.repository;

import com.stark.steadyai.entity.UrgeLog;
import com.stark.steadyai.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UrgeLogRepository extends JpaRepository<UrgeLog, Long> {

    List<UrgeLog> findByUserOrderByCreatedAtDesc(User user);

    Optional<UrgeLog> findByIdAndUser(Long id, User user);

    List<UrgeLog> findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(User user, java.time.LocalDateTime start, java.time.LocalDateTime end);

    List<UrgeLog> findByUserAndCreatedAtBetweenOrderByCreatedAtAsc(User user, java.time.LocalDateTime start, java.time.LocalDateTime end);

    List<UrgeLog> findByUser(User user);
}
