package com.learningplatform.learning.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Représente un parcours pédagogique créé par un professeur.
 * Règles métier :
 * - Un parcours DRAFT ne peut pas être assigné à un élève
 * - Seul un parcours VALIDATED peut être assigné
 * - La validation nécessite un titre non vide
 */
public class LearningPath {
    private UUID pathId;
    private String title;
    private String description;
    private UUID teacherId;
    private PathStatus status;
    private LocalDateTime createdAt;

    public LearningPath(UUID pathId, String title, String description, UUID teacherId, PathStatus status, LocalDateTime createdAt) {
        this.pathId = pathId;
        this.title = title;
        this.description = description;
        this.teacherId = teacherId;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UUID getPathId() {
        return pathId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public UUID getTeacherId() {
        return teacherId;
    }

    public PathStatus getStatus() {
        return status;
    }

    public void setStatus(PathStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean canBeAssigned() {
        return status == PathStatus.VALIDATED;
    }
}
