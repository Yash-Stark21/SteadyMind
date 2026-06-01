package com.stark.steadyai.entity;

import com.stark.steadyai.enums.CoachIntent;
import com.stark.steadyai.enums.ResponseType;
import com.stark.steadyai.enums.RiskLevel;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_messages")
public class AiMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many AI messages belong to one user
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private AiConversation conversation;

    @Column(nullable = false, length = 1000)
    private String userMessage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CoachIntent intent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RiskLevel riskLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ResponseType responseType;

    @Column(length = 2000)
    private String aiResponse;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public AiMessage() {
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public AiConversation getConversation() {
        return conversation;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public CoachIntent getIntent() {
        return intent;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public ResponseType getResponseType() {
        return responseType;
    }

    public String getAiResponse() {
        return aiResponse;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setConversation(AiConversation conversation) {
        this.conversation = conversation;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public void setIntent(CoachIntent intent) {
        this.intent = intent;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public void setResponseType(ResponseType responseType) {
        this.responseType = responseType;
    }

    public void setAiResponse(String aiResponse) {
        this.aiResponse = aiResponse;
    }
}
