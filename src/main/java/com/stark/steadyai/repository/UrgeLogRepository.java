package com.stark.steadyai.repository;

import com.stark.steadyai.entity.UrgeLog;
import com.stark.steadyai.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UrgeLogRepository extends JpaRepository<UrgeLog, Long> {

    List<UrgeLog> findByUserOrderByCreatedAtDesc(User user);
}
