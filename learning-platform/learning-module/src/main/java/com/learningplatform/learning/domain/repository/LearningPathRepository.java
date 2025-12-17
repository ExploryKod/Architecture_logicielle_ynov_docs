package com.learningplatform.learning.domain.repository;

import com.learningplatform.learning.domain.model.LearningPath;

import java.util.List;
import java.util.UUID;

/**
 * Interface Repository pour la gestion des parcours d'apprentissage.
 * Appartient strictement au domaine et permet d'abstraire l'accès aux données.
 * Les méthodes reflètent l'intention métier, pas l'opération technique.
 */
public interface LearningPathRepository {
    
    /**
     * Sauvegarde un parcours métier.
     */
    void savePath(LearningPath path);
    
    /**
     * Récupère un parcours métier par son identifiant.
     * @return le parcours ou null si absent
     */
    LearningPath findPathById(UUID pathId);
    
    /**
     * Liste les parcours prêts pour assignation (status VALIDATED).
     */
    List<LearningPath> findValidatedPaths();
    
    /**
     * Trouve les parcours en construction d'un professeur (brouillon).
     */
    List<LearningPath> findDraftsByTeacher(UUID teacherId);
    
    /**
     * Archive un parcours (change le status à ARCHIVED).
     */
    void archivePath(UUID pathId);
}
