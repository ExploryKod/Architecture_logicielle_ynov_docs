package com.learningplatform.learning.infrastructure.factory;

import com.learningplatform.learning.domain.repository.LearningPathRepository;
import com.learningplatform.learning.domain.repository.PathAssignmentRepository;
import com.learningplatform.learning.infrastructure.repository.InMemoryLearningPathRepository;
import com.learningplatform.learning.infrastructure.repository.InMemoryPathAssignmentRepository;

/**
 * Factory globale pour la création des repositories techniques.
 * 
 * Cette Factory centralise tous les "new" des repositories techniques,
 * garantissant un point d'instanciation unique et lisible.
 * 
 * Pattern Singleton : garantit un point d'accès unique à la Factory.
 * 
 * La Factory crée exclusivement des objets techniques (repositories),
 * jamais d'objets métier (LearningPath, PathAssignment, etc.).
 */
public class RepositoryFactory {
    
    private static RepositoryFactory instance;
    
    /**
     * Constructeur privé pour empêcher l'instanciation directe.
     * Force l'utilisation de getInstance().
     */
    private RepositoryFactory() {
        // Constructeur privé pour le pattern Singleton
    }
    
    /**
     * Retourne l'instance unique de la Factory (pattern Singleton).
     * 
     * @return l'instance unique de RepositoryFactory
     */
    public static RepositoryFactory getInstance() {
        if (instance == null) {
            instance = new RepositoryFactory();
        }
        return instance;
    }
    
    /**
     * Crée une instance de LearningPathRepository.
     * 
     * Point d'instanciation centralisé : tous les "new" de LearningPathRepository
     * sont regroupés ici. Un changement d'implémentation (InMemory à JPA) ne nécessite
     * qu'une modification dans cette méthode.
     * 
     * @return une instance de LearningPathRepository
     */
    public LearningPathRepository createLearningPathRepository() {
        return new InMemoryLearningPathRepository();
    }
    
    /**
     * Crée une instance de PathAssignmentRepository.
     * 
     * Point d'instanciation centralisé : tous les "new" de PathAssignmentRepository
     * sont regroupés ici. Un changement d'implémentation (InMemory à JPA) ne nécessite
     * qu'une modification dans cette méthode.
     * 
     * @return une instance de PathAssignmentRepository
     */
    public PathAssignmentRepository createPathAssignmentRepository() {
        return new InMemoryPathAssignmentRepository();
    }
}
