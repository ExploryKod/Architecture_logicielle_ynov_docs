package com.learningplatform.learning.infrastructure.repository;

import com.learningplatform.learning.domain.model.LearningPath;
import com.learningplatform.learning.domain.model.PathStatus;
import com.learningplatform.learning.domain.repository.LearningPathRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implémentation InMemory du repository LearningPathRepository.
 * Stockage en mémoire des parcours avec une Map.
 * Aucune dépendance à une base de données, ORM ou configuration externe.
 */
public class InMemoryLearningPathRepository implements LearningPathRepository {
    
    private final Map<UUID, LearningPath> storage = new HashMap<>();
    
    @Override
    public void savePath(LearningPath path) {
        if (path.getPathId() == null) {
            // Génération d'UUID pour les nouveaux parcours
            UUID newPathId = UUID.randomUUID();
            LearningPath newPath = new LearningPath(
                newPathId,
                path.getTitle(),
                path.getDescription(),
                path.getTeacherId(),
                path.getStatus(),
                path.getCreatedAt()
            );
            storage.put(newPathId, newPath);
        } else {
            // Mise à jour d'un parcours existant
            storage.put(path.getPathId(), path);
        }
    }
    
    @Override
    public LearningPath findPathById(UUID pathId) {
        return storage.get(pathId);
    }
    
    @Override
    public List<LearningPath> findValidatedPaths() {
        return storage.values().stream()
            .filter(path -> path.getStatus() == PathStatus.VALIDATED)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<LearningPath> findDraftsByTeacher(UUID teacherId) {
        return storage.values().stream()
            .filter(path -> path.getTeacherId().equals(teacherId))
            .filter(path -> path.getStatus() == PathStatus.DRAFT)
            .collect(Collectors.toList());
    }
    
    @Override
    public void archivePath(UUID pathId) {
        LearningPath path = storage.get(pathId);
        if (path != null) {
            path.setStatus(PathStatus.ARCHIVED);
            storage.put(pathId, path);
        }
    }
}
