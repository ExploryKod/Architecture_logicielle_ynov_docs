package com.learningplatform.learning.infrastructure.repository;

import com.learningplatform.learning.domain.model.PathAssignment;
import com.learningplatform.learning.domain.repository.PathAssignmentRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implémentation InMemory du repository PathAssignmentRepository.
 * Stockage en mémoire des assignations avec une Map.
 * Aucune dépendance à une base de données, ORM ou configuration externe.
 */
public class InMemoryPathAssignmentRepository implements PathAssignmentRepository {
    
    private final Map<UUID, PathAssignment> storage = new HashMap<>();
    
    @Override
    public void assignPathToStudent(UUID pathId, UUID studentId) {
        UUID assignmentId = UUID.randomUUID();
        PathAssignment assignment = new PathAssignment(
            assignmentId,
            pathId,
            studentId,
            LocalDateTime.now()
        );
        storage.put(assignmentId, assignment);
    }
    
    @Override
    public List<PathAssignment> findAssignmentsByStudent(UUID studentId) {
        return storage.values().stream()
            .filter(assignment -> assignment.getStudentId().equals(studentId))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<PathAssignment> findAssignmentsByPath(UUID pathId) {
        return storage.values().stream()
            .filter(assignment -> assignment.getPathId().equals(pathId))
            .collect(Collectors.toList());
    }
}
