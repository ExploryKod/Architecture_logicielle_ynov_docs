package com.learningplatform.learning;

import com.learningplatform.learning.application.service.PathAssignmentService;
import com.learningplatform.learning.domain.exception.BusinessRuleException;
import com.learningplatform.learning.domain.model.LearningPath;
import com.learningplatform.learning.domain.model.PathAssignment;
import com.learningplatform.learning.domain.model.PathStatus;
import com.learningplatform.learning.domain.repository.LearningPathRepository;
import com.learningplatform.learning.domain.repository.PathAssignmentRepository;
import com.learningplatform.learning.infrastructure.factory.RepositoryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour PathAssignmentService.
 * Ces tests démontrent le découplage métier/stockage et métier/création :
 * - Aucune base de données démarrée
 * - Aucune configuration datasource
 * - Aucun ORM initialisé
 * - Aucun serveur applicatif
 * 
 * Les repositories sont créés via la Factory globale (RepositoryFactory),
 * démontrant le point d'instanciation centralisé.
 * Le métier ne connaît plus les classes concrètes (InMemory*), seulement les interfaces.
 */
@DisplayName("Tests unitaires PathAssignmentService - Découplage métier/stockage")
class PathAssignmentServiceTest {
    
    private LearningPathRepository learningPathRepository;
    private PathAssignmentRepository pathAssignmentRepository;
    private PathAssignmentService pathAssignmentService;
    
    @BeforeEach
    void setUp() {
        // Utilisation de la Factory globale pour créer les repositories
        // Le métier ne connaît plus les classes concrètes (InMemory*)
        // Tous les "new" sont centralisés dans la Factory
        RepositoryFactory factory = RepositoryFactory.getInstance();
        
        learningPathRepository = factory.createLearningPathRepository();
        pathAssignmentRepository = factory.createPathAssignmentRepository();
        
        pathAssignmentService = new PathAssignmentService(
            learningPathRepository,
            pathAssignmentRepository
        );
    }
    
    @Test
    @DisplayName("Test 1 - Cas négatif : un parcours DRAFT ne peut pas être assigné à un élève")
    void testAssignDraftPath_ShouldThrowBusinessRuleException() {
        // Given (état initial)
        UUID teacherId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID pathId = UUID.randomUUID();
        
        // Créer un LearningPath avec status DRAFT
        LearningPath draftPath = new LearningPath(
            pathId,
            "Parcours Python - Brouillon",
            "Introduction à Python",
            teacherId,
            PathStatus.DRAFT,
            LocalDateTime.now()
        );
        
        // Sauvegarder ce parcours via le repository InMemory
        learningPathRepository.savePath(draftPath);
        
        // When (action métier) - Then (résultat attendu)
        // Une exception BusinessRuleException est levée
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> pathAssignmentService.assignPathToStudent(pathId, studentId),
            "Une exception BusinessRuleException doit être levée pour un parcours DRAFT"
        );
        
        // Vérifier que le message d'erreur contient "non validé" ou équivalent
        assertTrue(
            exception.getMessage().contains("non validé") || 
            exception.getMessage().contains("validé") ||
            exception.getMessage().contains("VALIDATED"),
            "Le message d'erreur doit indiquer que le parcours n'est pas validé"
        );
        
        // Vérifier qu'aucune assignation n'a été créée
        List<PathAssignment> assignments = pathAssignmentRepository.findAssignmentsByStudent(studentId);
        assertTrue(
            assignments.isEmpty(),
            "Aucune assignation ne doit avoir été créée pour un parcours DRAFT"
        );
    }
    
    @Test
    @DisplayName("Test 2 - Cas positif : un parcours VALIDATED peut être assigné avec succès")
    void testAssignValidatedPath_ShouldSucceed() {
        // Given (état initial)
        UUID teacherId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID pathId = UUID.randomUUID();
        
        // Créer un LearningPath avec status VALIDATED
        LearningPath validatedPath = new LearningPath(
            pathId,
            "Parcours Python - Introduction",
            "Introduction complète à Python",
            teacherId,
            PathStatus.VALIDATED,
            LocalDateTime.now()
        );
        
        // Sauvegarder via le repository InMemory
        learningPathRepository.savePath(validatedPath);
        
        // When (action métier)
        // Aucune exception ne doit être levée
        assertDoesNotThrow(
            () -> pathAssignmentService.assignPathToStudent(pathId, studentId),
            "Aucune exception ne doit être levée pour un parcours VALIDATED"
        );
        
        // Then (résultat attendu)
        // Vérifier que la liste retournée contient exactement 1 assignation
        List<PathAssignment> assignments = pathAssignmentRepository.findAssignmentsByStudent(studentId);
        assertEquals(
            1,
            assignments.size(),
            "Une assignation doit avoir été créée"
        );
        
        // Vérifier que l'assignation contient le pathId correct
        PathAssignment assignment = assignments.get(0);
        assertEquals(
            pathId,
            assignment.getPathId(),
            "L'assignation doit contenir le pathId correct"
        );
        
        // Vérifier que l'assignation contient le studentId correct
        assertEquals(
            studentId,
            assignment.getStudentId(),
            "L'assignation doit contenir le studentId correct"
        );
        
        // Vérifier que assignedAt est non null et récent
        assertNotNull(
            assignment.getAssignedAt(),
            "assignedAt ne doit pas être null"
        );
        
        // Vérifier que assignedAt est récent (moins de 5 secondes)
        LocalDateTime now = LocalDateTime.now();
        assertTrue(
            assignment.getAssignedAt().isBefore(now.plusSeconds(5)) &&
            assignment.getAssignedAt().isAfter(now.minusSeconds(5)),
            "assignedAt doit être récent"
        );
    }
    
    @Test
    @DisplayName("Test supplémentaire : un parcours ARCHIVED ne peut pas être assigné")
    void testAssignArchivedPath_ShouldThrowBusinessRuleException() {
        // Given
        UUID teacherId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID pathId = UUID.randomUUID();
        
        LearningPath archivedPath = new LearningPath(
            pathId,
            "Parcours Python - Archivé",
            "Ancien parcours",
            teacherId,
            PathStatus.ARCHIVED,
            LocalDateTime.now()
        );
        
        learningPathRepository.savePath(archivedPath);
        
        // When - Then
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> pathAssignmentService.assignPathToStudent(pathId, studentId)
        );
        
        assertTrue(exception.getMessage().contains("non validé") || 
                   exception.getMessage().contains("validé"));
        
        List<PathAssignment> assignments = pathAssignmentRepository.findAssignmentsByStudent(studentId);
        assertTrue(assignments.isEmpty());
    }
    
    @Test
    @DisplayName("Test supplémentaire : assignation d'un parcours inexistant doit lever une exception")
    void testAssignNonExistentPath_ShouldThrowBusinessRuleException() {
        // Given
        UUID studentId = UUID.randomUUID();
        UUID nonExistentPathId = UUID.randomUUID();
        
        // When - Then
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> pathAssignmentService.assignPathToStudent(nonExistentPathId, studentId)
        );
        
        assertTrue(exception.getMessage().contains("n'existe pas"));
    }
}
