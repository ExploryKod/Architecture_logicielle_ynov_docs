package com.learningplatform.learning.application.service;

import com.learningplatform.learning.domain.exception.BusinessRuleException;
import com.learningplatform.learning.domain.model.LearningPath;
import com.learningplatform.learning.domain.model.PathStatus;
import com.learningplatform.learning.domain.repository.LearningPathRepository;
import com.learningplatform.learning.domain.repository.PathAssignmentRepository;

import java.util.UUID;

/**
 * Service orchestrant les assignations de parcours.
 * Dépend uniquement des abstractions (interfaces Repository), pas des implémentations concrètes.
 */
public class PathAssignmentService {
    
    private final LearningPathRepository learningPathRepository;
    private final PathAssignmentRepository pathAssignmentRepository;
    
    public PathAssignmentService(
            LearningPathRepository learningPathRepository,
            PathAssignmentRepository pathAssignmentRepository) {
        this.learningPathRepository = learningPathRepository;
        this.pathAssignmentRepository = pathAssignmentRepository;
    }
    
    /**
     * Assigne un parcours à un élève.
     * Règle métier critique : un parcours ne peut être assigné que s'il est VALIDATED.
     * 
     * @param pathId l'identifiant du parcours
     * @param studentId l'identifiant de l'élève
     * @throws BusinessRuleException si le parcours n'existe pas ou n'est pas validé
     */
    public void assignPathToStudent(UUID pathId, UUID studentId) {
        // Récupère le parcours via le repository
        LearningPath path = learningPathRepository.findPathById(pathId);
        
        // Vérifie que le parcours existe
        if (path == null) {
            throw new BusinessRuleException("Le parcours avec l'ID " + pathId + " n'existe pas");
        }
        
        // Vérifie que le parcours a le status VALIDATED (règle métier critique)
        if (path.getStatus() != PathStatus.VALIDATED) {
            throw new BusinessRuleException(
                "Impossible d'assigner le parcours : le parcours n'est pas validé (status: " + path.getStatus() + ")"
            );
        }
        
        // Si le status est VALIDATED, crée une PathAssignment
        pathAssignmentRepository.assignPathToStudent(pathId, studentId);
    }
}
