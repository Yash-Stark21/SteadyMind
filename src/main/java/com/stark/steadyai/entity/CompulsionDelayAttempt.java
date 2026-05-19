package com.stark.steadyai.entity;

import com.stark.steadyai.enums.CompulsionDelayOutcome;
import com.stark.steadyai.enums.CopingStrategy;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "compulsion_delay_attempts")
public class CompulsionDelayAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "urge_log_id")
    private UrgeLog urgeLog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exposure_task_id")
    private ExposureTask exposureTask;

    @Column(length = 500)
    private String triggerDescription;

    @Column(nullable = false)
    private Integer plannedDelayMinutes;

    private Integer actualDelayMinutes;

    @Enumerated(EnumType.STRING)
    private CompulsionDelayOutcome outcome;

    @Enumerated(EnumType.STRING)
    private CopingStrategy copingStrategyUsed;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public CompulsionDelayAttempt() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public UrgeLog getUrgeLog() {
        return urgeLog;
    }

    public void setUrgeLog(UrgeLog urgeLog) {
        this.urgeLog = urgeLog;
    }

    public ExposureTask getExposureTask() {
        return exposureTask;
    }

    public void setExposureTask(ExposureTask exposureTask) {
        this.exposureTask = exposureTask;
    }

    public String getTriggerDescription() {
        return triggerDescription;
    }

    public void setTriggerDescription(String triggerDescription) {
        this.triggerDescription = triggerDescription;
    }

    public Integer getPlannedDelayMinutes() {
        return plannedDelayMinutes;
    }

    public void setPlannedDelayMinutes(Integer plannedDelayMinutes) {
        this.plannedDelayMinutes = plannedDelayMinutes;
    }

    public Integer getActualDelayMinutes() {
        return actualDelayMinutes;
    }

    public void setActualDelayMinutes(Integer actualDelayMinutes) {
        this.actualDelayMinutes = actualDelayMinutes;
    }

    public CompulsionDelayOutcome getOutcome() {
        return outcome;
    }

    public void setOutcome(CompulsionDelayOutcome outcome) {
        this.outcome = outcome;
    }

    public CopingStrategy getCopingStrategyUsed() {
        return copingStrategyUsed;
    }

    public void setCopingStrategyUsed(CopingStrategy copingStrategyUsed) {
        this.copingStrategyUsed = copingStrategyUsed;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
