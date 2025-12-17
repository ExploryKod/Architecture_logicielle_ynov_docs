package com.learningplatform.learning.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Représente l'assignation d'un parcours à un élève.
 * Règle métier : Une assignation ne peut être créée que si le parcours (LearningPath) est VALIDATED
 */
public class PathAssignment {
    private UUID assignmentId;
    private UUID pathId;
    private UUID studentId;
    private LocalDateTime assignedAt;

    public PathAssignment(UUID assignmentId, UUID pathId, UUID studentId, LocalDateTime assignedAt) {
        this.assignmentId = assignmentId;
        this.pathId = pathId;
        this.studentId = studentId;
        this.assignedAt = assignedAt;
    }

    public UUID getAssignmentId() {
        return assignmentId;
    }

    public UUID getPathId() {
        return pathId;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }
}
