# Séance 9 - Rendre l'application dynamique

### Contexte et philosophie du projet

Notre application sert pour rappel à créer des parcours personnalisé sur-mesure afin de faciliter l'apprentissage (techniques de mémorisation, adaptation au profil etc.) des élèves de la plateforme. Elle se base pour cela sur une collaboration étroite entre élève, professeur et IA. L'IA ainsi humanisé, dans sa dimension coopérative, préfigure les usages intelligent de l'IA et non l'IA impersonnel, non-supervisé qui détruirait les métiers (ici de professeur et rend l'élève seul face à son IA-professeur). 

Notre projet modifie en profondeur le métier de professeur mais en réalité il brise le silence : cela fait déjà plus de cinquante ans voir plus que le métier de professeur n'est plus celui qui nous est imposé et nous sommes passé à côté du vrai métier de professeur. L'impact de l'IA, bien plus que n'importe quel politique publique, est peut-être à même de faire évoluer le Mammouth vers son âge réel en dehors de la période néolithique de l'éducation pour rester sur la métaphore et l'hyperbole. 

L'innovation pédagogique quand on l'évoque aujourd'hui n'est en réalité trés souvent qu'une confusion avec ajout de nouvelles technologies dans un contexte qui n'a rien changé dans la mentalité en profondeur. Cette plateforme pourrait n'être que ça.

Ce n'est donc pas une simple plateforme d'IA comme il y en a plein d'autres, c'est un projet pédagogique complexe qui implique l'humain et donc toute les sciences en lien. Une équipe de développeur et son PO n'est pas à même de mener un tel projet sans l'expérience d'autres professions, des écoles et des professeurs.

C'est pourquoi, rendre cette application dynamique implique : 
- De ne pas se contenter de modifier le métier en surface (typologies etc...) mais de rendre compte d'une philosophie pédagogique nouvelle. 

### Problématisation et outils

Nous prenons pour exemple l'assignation d'un parcours d'apprentissage (LearningPath). Nous avons deux problèmes A et B.
- (A) Plusieurs niveau pour un même parcours pose problème : on pourrait mettre dans chaque parcours sa méthode de gestion des niveaux avec des if/else complexe selon le parcours. Il y a une multiplicité de parcours variables : ils varient car ils sont créé sur-mesure selon un ensemble de profil d'élèves. Ils varient aussi en terme de niveaux. Comment assigner un niveau cohérent - créer par ailleurs par l'école 
Exemple avec le TOEIC : A1, B2, C1 ont une signification unifié, lisible sur le marché de l'emploi. Nous voulons créer la même lisibilité dans l'application. Or nos parcours eux sont différents. On serait tenté de mettre dans chaque parcours son niveau avec ses méthodes spécifiques. On se retrouve avec des centaines de if/else répartie dans chaque parcours quand l'application prend de l'ampleur et chacun créer ses catégories de parcours.
- (B) Il faut en plus rapporter au professeur des informations diverses au regard de chaque parcours qui sont des notifications pour alerter sur le suivi du parcours. On ne peux pas non plus mettre cela partout et se répéter en plus car nous n'avons pas de pattern capable d'unifier ces notification dans une logique Open/Closed principle qui garantie l'intégrité de la règle métier (sa stabilité malgré ce qui varit).

Deux patterns permettent de répondre :
- Le pattern strategy qui va réduire la complexité inutile de ce qui est variable dans le métier sans connaître les détails d'implémentation. Il modifie une régle existante de base sans la remplacer mais le résultat est différent selon les différentes stratégies car ce résultat dépend du facteur variable. 
- Le pattern décorator qui permet d'enrichir le métier dans la simplicité : contrairement à la strategy, il ne modifie pas la règle métier, il ajoute un plus.

Dans les deux cas ils permettent de rendre l'application testable, maintenable, robuste, découplé.... et consolide le respect de tous les piliers du développement logiciel sans dette technique.

### Résolution de la problématique : pseudo-code sur notre exemple (strategy + decorator)

#### 1. Pattern Strategy : résoudre le problème A

**Interface Strategy** : Décision métier pure sur l'éligibilité d'un niveau

```pseudo-code
interface LevelEligibilityStrategy {
    canAssignLevel(path: LearningPath, student: Student, level: Level) -> boolean
    // Décision pure : reçoit les données déjà chargées, ne fait QUE décider
    // Pas d'appel repository, pas d'appel API
}
```

**Pourquoi cette interface répond au problème A** : Elle centralise la décision d'éligibilité de niveau dans une interface unique. Au lieu d'avoir des if/else dispersés dans chaque parcours, toutes les règles de niveau sont regroupées dans des stratégies isolées et remplaçables.

**Implémentation 1 : Strategy basée sur le niveau standard (TOEIC-like)**

```pseudo-code
class StandardLevelEligibilityStrategy implements LevelEligibilityStrategy {
    canAssignLevel(path: LearningPath, student: Student, level: Level) -> boolean:
        // Règle métier : l'élève doit avoir le niveau requis ou le niveau immédiatement inférieur
        studentLevel = student.getCurrentLevel()
        requiredLevel = path.getRequiredLevel()
        
        return studentLevel >= requiredLevel 
            or studentLevel == getPreviousLevel(requiredLevel)
        // Décision simple : comparaison de niveaux standardisés (A1, B2, C1)
}
```

**Pourquoi cette stratégie répond au problème A** : Elle implémente une règle standardisée comme le TOEIC. Le niveau est comparé de manière unifiée (A1 < B2 < C1), créant la lisibilité souhaitée sur le marché. Plus besoin de if/else spécifiques par parcours.

**Implémentation 2 : Strategy basée sur le niveau personnalisé par école**

```pseudo-code
class SchoolCustomLevelEligibilityStrategy implements LevelEligibilityStrategy {
    canAssignLevel(path: LearningPath, student: Student, level: Level) -> boolean:
        // Règle métier : utilise le référentiel de niveaux de l'école
        schoolLevelSystem = student.getSchool().getLevelSystem()
        studentLevel = student.getCurrentLevelInSchoolSystem()
        requiredLevel = path.getRequiredLevelInSchoolSystem()
        
        return schoolLevelSystem.isEligible(studentLevel, requiredLevel)
        // Décision : utilise le système de niveaux propre à l'école
}
```

**Pourquoi cette stratégie répond au problème A** : Elle permet à chaque école d'avoir son propre référentiel de niveaux tout en gardant une interface unifiée. L'école définit sa logique de comparaison (ex: "Débutant", "Intermédiaire", "Avancé"), mais le code métier reste stable.

**Utilisation dans le service**

```pseudo-code
class PathAssignmentService {
    constructor(
        learningPathRepository: LearningPathRepository,
        pathAssignmentRepository: PathAssignmentRepository,
        levelEligibilityStrategy: LevelEligibilityStrategy  // Strategy injectée
    )
    
    assignPathToStudent(pathId: UUID, studentId: UUID, level: Level):
        // 1. Le service charge les données (I/O)
        path = learningPathRepository.findPathById(pathId)
        student = userService.findStudentById(studentId)
        
        // 2. La Strategy décide uniquement (pas d'I/O)
        if not levelEligibilityStrategy.canAssignLevel(path, student, level):
            throw BusinessRuleException("Le niveau n'est pas éligible pour cet élève")
        
        // 3. Le service exécute l'assignation
        pathAssignmentRepository.assignPathToStudent(pathId, studentId, level)
}
```

**Pourquoi cette utilisation répond au problème A** : Le service ne contient plus de if/else sur les niveaux. Il délègue la décision à la Strategy. Pour ajouter une nouvelle règle de niveau (ex: niveau par compétence), on crée une nouvelle Strategy sans modifier le service.

#### 2. Pattern Decorator : résoudre le problème B

**Service de base (règle métier pure - ne change jamais)**

```pseudo-code
class PathAssignmentService {
    constructor(
        learningPathRepository: LearningPathRepository,
        pathAssignmentRepository: PathAssignmentRepository
    )
    
    assignPathToStudent(pathId: UUID, studentId: UUID) -> AssignmentResult:
        // Règle métier : validation + assignation
        // Cette logique ne change JAMAIS
        path = learningPathRepository.findPathById(pathId)
        
        if path == null:
            throw BusinessRuleException("Parcours inexistant")
        
        if path.getStatus() != VALIDATED:
            throw BusinessRuleException("Parcours non validé")
        
        pathAssignmentRepository.assignPathToStudent(pathId, studentId)
        return AssignmentResult(success: true, pathId, studentId)
}
```

**Pourquoi ce service répond au problème B** : La règle métier d'assignation est isolée et stable. Elle ne contient aucune logique de notification, ce qui permet d'enrichir le comportement sans la modifier.

**Decorator 1 : Notification au professeur**

```pseudo-code
class TeacherNotificationDecorator extends PathAssignmentService {
    notificationService: NotificationService
    wrapped: PathAssignmentService
    
    constructor(wrapped: PathAssignmentService, notificationService: NotificationService):
        this.wrapped = wrapped
        this.notificationService = notificationService
    
    assignPathToStudent(pathId: UUID, studentId: UUID) -> AssignmentResult:
        // Appel de la règle métier de base (NE CHANGE JAMAIS)
        result = wrapped.assignPathToStudent(pathId, studentId)
        
        // Enrichissement : notification au professeur après assignation
        if result.success:
            teacherId = path.getTeacherId()
            notificationService.notifyTeacher(teacherId, 
                "Un parcours a été assigné à un élève", {pathId, studentId})
        
        return result
}
```

**Pourquoi ce Decorator répond au problème B** : Il ajoute la notification au professeur sans modifier la règle métier. Le service de base reste inchangé, respectant le principe Open/Closed.

**Decorator 2 : Notification d'alerte sur le suivi**

```pseudo-code
class ProgressAlertNotificationDecorator extends PathAssignmentService {
    notificationService: NotificationService
    wrapped: PathAssignmentService
    
    assignPathToStudent(pathId: UUID, studentId: UUID) -> AssignmentResult:
        result = wrapped.assignPathToStudent(pathId, studentId)
        
        // Enrichissement : alerte si l'élève a déjà plusieurs parcours en cours
        if result.success:
            activePaths = pathAssignmentRepository.countActivePaths(studentId)
            if activePaths > 3:
                teacherId = path.getTeacherId()
                notificationService.notifyTeacher(teacherId,
                    "Alerte : l'élève a maintenant " + activePaths + " parcours actifs", 
                    {studentId, activePaths})
        
        return result
}
```

**Pourquoi ce Decorator répond au problème B** : Il ajoute une notification d'alerte spécifique au suivi sans toucher à la règle métier. On peut empiler plusieurs Decorators pour combiner différentes notifications.

**Decorator 3 : Logs métier**

```pseudo-code
class LoggingPathAssignmentDecorator extends PathAssignmentService {
    logger: Logger
    wrapped: PathAssignmentService
    
    assignPathToStudent(pathId: UUID, studentId: UUID) -> AssignmentResult:
        // Log AVANT l'assignation
        logger.info("Assignation de parcours demandée", {pathId, studentId})
        
        result = wrapped.assignPathToStudent(pathId, studentId)
        
        // Log APRÈS l'assignation
        logger.info("Assignation terminée", {pathId, studentId, success: result.success})
        
        return result
}
```

**Pourquoi ce Decorator répond au problème B** : Il ajoute la traçabilité métier sans modifier la règle d'assignation. Les logs enrichissent le comportement sans le changer.

**Composition (empilement des Decorators)**

```pseudo-code
// Service de base (règle métier pure)
baseService = new PathAssignmentService(repository, assignmentRepo)

// Enrichissements empilés (la règle métier n'est jamais modifiée)
enrichedService = new LoggingPathAssignmentDecorator(
    new TeacherNotificationDecorator(
        new ProgressAlertNotificationDecorator(
            baseService  // Règle métier pure au centre
        )
    )
)

// Utilisation : la règle métier est enrichie mais jamais modifiée
enrichedService.assignPathToStudent(pathId, studentId)
```

**Pourquoi cette composition répond au problème B** : On peut ajouter ou retirer des notifications sans modifier le service de base. Chaque Decorator fait une chose précise (logs, notification professeur, alerte suivi), et on les combine selon les besoins. Plus de répétition de code, respect du principe Open/Closed.

### Schéma d'intégration avec la Facade

```
LearningPathFacade
    ↓
PathAssignmentService (enrichi avec Decorators)
    ↓
LoggingDecorator → TeacherNotificationDecorator → ProgressAlertNotificationDecorator
    ↓
PathAssignmentService (règle métier pure)
    ↓
LevelEligibilityStrategy (décision sur les niveaux)
    ↓
Repository
```

**Pourquoi cette intégration est cohérente** : La Facade (séance 8) utilise le service enrichi sans connaître les détails. Elle bénéficie automatiquement des notifications et logs sans modification. La Strategy est utilisée en interne par le service, invisible pour la Facade.
