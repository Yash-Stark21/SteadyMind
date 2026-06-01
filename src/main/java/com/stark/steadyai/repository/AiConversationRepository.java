package com.stark.steadyai.repository;

import com.stark.steadyai.entity.AiConversation;
import com.stark.steadyai.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiConversationRepository extends JpaRepository<AiConversation, Long> {

    Optional<AiConversation> findFirstByUserAndActiveTrueOrderByStartedAtDesc(User user);

    Optional<AiConversation> findByIdAndUser(Long id, User user);

    List<AiConversation> findByUserOrderByUpdatedAtDesc(User user);

    List<AiConversation> findByUserAndActiveFalseOrderByEndedAtDesc(User user);
}
