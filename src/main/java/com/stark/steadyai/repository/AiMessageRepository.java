package com.stark.steadyai.repository;

import com.stark.steadyai.entity.AiMessage;
import com.stark.steadyai.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiMessageRepository extends JpaRepository<AiMessage, Long> {

    List<AiMessage> findByUserOrderByCreatedAtDesc(User user);
}
