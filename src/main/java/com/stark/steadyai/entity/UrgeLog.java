package com.stark.steadyai.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "urge_logs")
public class UrgeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many urge logs belong to one user
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 500)
    private String triggerText;

    @Column(length = 500)
    private String obsessionText;

    @Column(nullable = false, length = 300)
    private String compulsionUrge;

    @Column(nullable = false)
    private Integer intensityBefore;

    private Integer intensityAfter;

    @Column(nullable = false)
    private Integer delayMinutes;

    @Column(nullable = false)
    private Boolean compulsionPerformed;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public UrgeLog() {
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getTriggerText() {
        return triggerText;
    }

    public String getObsessionText() {
        return obsessionText;
    }

    public String getCompulsionUrge() {
        return compulsionUrge;
    }

    public Integer getIntensityBefore() {
        return intensityBefore;
    }

    public Integer getIntensityAfter() {
        return intensityAfter;
    }

    public Integer getDelayMinutes() {
        return delayMinutes;
    }

    public Boolean getCompulsionPerformed() {
        return compulsionPerformed;
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

    public void setTriggerText(String triggerText) {
        this.triggerText = triggerText;
    }

    public void setObsessionText(String obsessionText) {
        this.obsessionText = obsessionText;
    }

    public void setCompulsionUrge(String compulsionUrge) {
        this.compulsionUrge = compulsionUrge;
    }

    public void setIntensityBefore(Integer intensityBefore) {
        this.intensityBefore = intensityBefore;
    }

    public void setIntensityAfter(Integer intensityAfter) {
        this.intensityAfter = intensityAfter;
    }

    public void setDelayMinutes(Integer delayMinutes) {
        this.delayMinutes = delayMinutes;
    }

    public void setCompulsionPerformed(Boolean compulsionPerformed) {
        this.compulsionPerformed = compulsionPerformed;
    }
}
