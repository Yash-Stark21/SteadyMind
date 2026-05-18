package com.stark.steadyai.entity;

import com.stark.steadyai.enums.ExposureStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exposure_items")
public class ExposureItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many exposure items belong to one user
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Integer anxietyRating;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ExposureStatus status = ExposureStatus.PLANNED;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = ExposureStatus.PLANNED;
        }
    }

    public ExposureItem() {
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Integer getAnxietyRating() {
        return anxietyRating;
    }

    public ExposureStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAnxietyRating(Integer anxietyRating) {
        this.anxietyRating = anxietyRating;
    }

    public void setStatus(ExposureStatus status) {
        this.status = status;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
