package com.learningplatform.learning.domain.repository;

import com.learningplatform.learning.domain.model.PathAssignment;

import java.util.List;
import java.util.UUID;

/**
 * Interface Repository pour la gestion des assignations de parcours aux élèves.
 * Appartient strictement au domaine et permet d'abstraire l'accès aux données.
 */
public interface PathAssignmentRepository {
    
    /**
     * Assigne un parcours à un élève.
     */
    void assignPathToStudent(UUID pathId, UUID studentId);
    
    /**
     * Liste les parcours assignés à un élève.
     */
    List<PathAssignment> findAssignmentsByStudent(UUID studentId);
    
    /**
     * Liste les élèves assignés à un parcours.
     */
    List<PathAssignment> findAssignmentsByPath(UUID pathId);
}
