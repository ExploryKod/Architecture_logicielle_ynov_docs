# Livrable Séance 5 - Repositories/DAO

## Consignes (rappel)

### À la fin du livrable, on doit retrouver dans le projet :
1.​ Une ou plusieurs interfaces (DAO et/ou Repository) représentant le contrat
d’accès aux données.
2.​ Une implémentation simulée de ce contrat (InMemory / Fake / Stub).
3.​ Une organisation claire dans l’arborescence (cohérente avec tes modules et
tes couches).
4.​ Un ou deux exemples de flux métier qui utilisent uniquement l’abstraction
(et pas le stockage).

## Contexte (rappel)

Le projet est un fournisseur d’espaces au sein de cette plateforme d’apprentissage qui agit comme un SAAS modulable. A l’image de Moodle Cloud qui fournit des espaces de “Learning Management System” (LMS) afin que les écoles personnalisent les espaces d’échanges avec les étudiants nous fournissons des espaces qui permettent de créer des parcours d’apprentissage sur-mesure avec des outils poussés de mémorisation personnalisable par élève, par matière et par professeur.


| **Module**               | **Classes clés**                                                                |
| ------------------------ | ------------------------------------------------------------------------------- |
| **module-users**         | User, StudentProfile, Role, Permission, UserService                             |
| **module-learning**      | LearningPath, LearningStep, RevisionSession, ContentItem, LearningService       |
| **module-billing**       | Subscription, Invoice, Payment, SchoolAccount, BillingService                   |
| **module-reporting**     | Dashboard, PerformanceIndicator, Report, StatisticsAggregator, ReportingService |
| **module-core**          | BaseEntity, NotificationService, SecurityManager, IAEngine, Utilities           |
| **module-communication** | Conversation, Message, RendezVous, Visioconference, Notification                |

## Exemples d'interfaces DAO et/ou Repository

Nous allons nous concentrer sur le module `module-learning` pour illustrer les interfaces. En effet il est riche en règles métiers et il est vraiment représentatif de notre projet. 

Nous allons extraire trois régles métiers clés de ce module et définir les interfaces nécessaires pour les implémenter. Ces interfaces représenteront le contrat d'accès aux données pour les entités concernées.

**Trois opérations métier sont retenues pour la démonstration :**

- Le professeur personnalise un parcours d'apprentissage pour un élève à partir de son profil (Contrainte métier : un parcours doit être adapté au profil de l'élève).
- Valider un parcours par un professeur (Contrainte métier : un parcours ne peut être assigné à un élève que s'il est validé).
- Lister les parcours validés disponibles pour assignation aux élèves (Contrainte métier : seuls les parcours validés peuvent être assignés).

A partir de ces opérations, nous extrayons les entités métiers concernées : 

#### Entité `LearningPath`
Représente un parcours pédagogique créé par un professeur.

**Attributs :** (Avec typage)

- pathId (identifiant unique) : UUID
- title (titre du parcours) : String
- description (description du parcours) : String
- teacherId (professeur créateur): UUID
- status (PathStatus enum : DRAFT, VALIDATED, ARCHIVED)
- createdAt (date de création) : DateTime

***Règles métier intégrées :***

- Un parcours DRAFT ne peut pas être assigné à un élève
- Seul un parcours VALIDATED peut être assigné
- La validation nécessite un titre non vide

***Focus sur l'Enum PathStatus :***

Valeurs possibles :

DRAFT (brouillon, en cours de construction)
VALIDATED (validé, prêt pour assignation)
ARCHIVED (archivé, non disponible)

#### Entité `PathAssignment`

Représente l'assignation d'un parcours à un élève.

**Attributs :** (Avec typage)

- assignmentId (identifiant unique): UUID
- pathId (référence au parcours): UUID
- studentId (identifiant élève) : UUID
- assignedAt (date d'assignation) : DateTime

***Règle métier :***

Une assignation ne peut être créée que si le parcours (LearningPath) est VALIDATED

Pour information, voici les autres entités métiers impliquées dans ces opérations :

### Entité `Student`
Représente un élève, incluant ses préférences et son historique d'apprentissage.

**Attributs :** (Avec typage)
- studentId (référence à l'élève) : UUID (PKey)
- profileId (identifiant unique) : UUID (Foreign Key vers StudentProfile)
- name (nom de l'élève) : String
- email (email de l'élève) : String
- learningPreferences (préférences d'apprentissage) : Map<String, String>
- completedPaths (parcours complétés) : List<UUID>
- studentAssignedPaths (parcours assignés) : List<UUID>

***Règle métier :***
Le profil doit être consulté pour personnaliser les parcours d'apprentissage.

### Entité `Teacher`
Représente un professeur qui crée et valide des parcours d'apprentissage.

**Attributs :** (Avec typage)
- teacherId (identifiant unique) : UUID (PK)
- name (nom du professeur) : String
- email (email du professeur) : String
- professorAssignedPaths (parcours supervisé par le professeur) : List<UUID>
- professorStudentList (liste des UUID des élèves) : List<UUID>

***Règle métier :***
Un professeur doit être authentifié pour créer ou valider des parcours.
Un professeur doit être authentifié pour ajouter des élèves.
Un professeur peut superviser plusieurs élèves.

Ces entités appartiennent au domaine, ils sont trés trés stable. Certains vont appartenir au core (car transverse comme Teacher ou Student) et d'autres au module-learning seulement. 

Si besoin nous pourrons utiliser des **DTOs** (Data Transfer Object) pour transporter les données entre les couches sans dépendre d'une entité de base mais ici nous nous concentrons sur le sujet Repository vs DAO.

### Interfaces : Repository.

Dans notre cas, nos n'implémentons pas de DAO. A ce stade de notre projet nous n'en avons pas besoin car nous n'allons pas dans une analyse trés Macro. 

Les Repository appartiennent strictement au domaine et permettent d'abstraire l'accès aux données. Il exprime des règles métier même si les termes peuvent être simple comme "save". Ils ne dépendent pas d'un ORM, ne dépendent pas d'une base de données, d'une technologie ou autre infrastructure.

Ils sont donc appellé par nos services métiers qui ne connaissant pas l'implémentation concrète. L'infrastructure fourni une implémentation technique de ces interfaces mais elle pourrait changer sans que les interfaces ne doivent elles changer. Les services n'ont pas non plus besoin de changer si l'implémentation technique change. C'est tout l'intérêt de cette couche d'abstraction qui permet l'inversion de dépendance entre infrastructure et domaine. Le domaine ne dépend pas de l'infrastructure mais l'infrastructure dépend du domaine.

#### Interface `LearningPathRepository`
Responsable de la gestion des parcours d'apprentissage.

**Méthodes orientées métier :**

- savePath(LearningPath path) - sauvegarde un parcours métier
- findPathById(UUID pathId) - récupère un parcours métier
- findValidatedPaths() - liste les parcours prêts pour assignation
- findDraftsByTeacher(UUID teacherId) - trouve parcours en construction d'un professeur (brouillon)
- archivePath(UUID pathId) - archive un parcours 

Retourne des objets métier purs (LearningPath du domaine).
Le nom des méthodes reflète l'intention métier, pas l'opération technique.

**Exemple en Java :**

Pour la lisibilité nous utilisons un exemple en Java mais cela peut s'écrire dans n'importe quel langage orienté objet.

```java
public interface LearningPathRepository {
    void savePath(LearningPath path);
    LearningPath findById(String pathId);
    List<LearningPath> findValidatedPaths();
    List<LearningPath> findDraftsByTeacher(UUID teacherId);
    void archivePath(UUID pathId);
}
```

#### Interface `PathAssignmentRepository`

Responsable de la gestion des assignations de parcours aux élèves.

**Méthodes orientées métier :**
- assignPathToStudent(UUID pathId, UUID studentId) - assigne un parcours à un élève
- findAssignmentsByStudent(UUID studentId) - liste les parcours assignés à un élève
- findAssignmentsByPath(UUID pathId) - liste les élèves assignés à un parcours

**Exemple en Java :**

```java
public interface PathAssignmentRepository {
    void assignPathToStudent(UUID pathId, UUID studentId);
    List<PathAssignment> findAssignmentsByStudent(UUID studentId);
    List<PathAssignment> findAssignmentsByPath(UUID pathId);
}
```

## Simulation - Exemple de InMemory 

Ce sont des objet qui serve à simuler une base de donnée en donnant une donnée structuré de la même manière que si il y avait une base de donnée. C'est utile pour tester l'implémentation. 

Ces InMemory sont dans l'infrastructure et dépendent du domaine (ce dernier ne dépend pas d'eux). Ils doivent passer via les interfaces du domaine pour produire une implémentation sans quoi ils ne "prennent pas vie". Ils ne peuvent donc pas par eux-même créer des règles métiers mais se content de les implémenter sur le plan  technique.

### Implémentation `InMemoryLearningPathRepository`

Cette classe implémente l'interface `LearningPathRepository` et fournit un stockage en mémoire des parcours d'apprentissage.

**Structure interne :**

- Map<UUID, LearningPath> storage : structure de stockage utilisant une HashMap avec UUID comme clé et LearningPath comme valeur

**Méthodes implémentées :**

- savePath(LearningPath path) : si le pathId est null, génère un nouvel UUID et crée un nouveau LearningPath avec cet identifiant, sinon met à jour le parcours existant dans la Map
- findPathById(UUID pathId) : retourne le parcours correspondant à l'UUID ou null si absent
- findValidatedPaths() : filtre les parcours stockés pour retourner uniquement ceux dont le status est VALIDATED
- findDraftsByTeacher(UUID teacherId) : filtre les parcours par teacherId et status DRAFT
- archivePath(UUID pathId) : récupère le parcours, modifie son status à ARCHIVED et le sauvegarde dans la Map

Aucune dépendance à une base de données, ORM ou configuration externe. Le stockage est temporaire et perdu à la fin de l'exécution.

### Implémentation `InMemoryPathAssignmentRepository`

Cette classe implémente l'interface `PathAssignmentRepository` et fournit un stockage en mémoire des assignations de parcours aux élèves.

**Structure interne :**

- Map<UUID, PathAssignment> storage : structure de stockage utilisant une HashMap avec UUID comme clé et PathAssignment comme valeur

**Méthodes implémentées :**

- assignPathToStudent(UUID pathId, UUID studentId) : génère un nouvel UUID pour l'assignation, crée une PathAssignment avec pathId, studentId et la date courante (LocalDateTime.now()), puis la stocke dans la Map
- findAssignmentsByStudent(UUID studentId) : filtre les assignations stockées pour retourner uniquement celles correspondant au studentId fourni
- findAssignmentsByPath(UUID pathId) : filtre les assignations stockées pour retourner uniquement celles correspondant au pathId fourni

Aucune dépendance à une base de données, ORM ou configuration externe. Le stockage est temporaire et perdu à la fin de l'exécution.

## Arborescence du module learning-module

L'arborescence suivante montre l'organisation du code du module learning-module avec la séparation claire des couches et l'inversion de dépendance.

```
learning-module/
├── pom.xml
└── src/
    ├── main/
    │   └── java/
    │       └── com/
    │           └── learningplatform/
    │               └── learning/
    │                   ├── domain/                          (COUCHE DOMAINE - Ne dépend de rien)
    │                   │   ├── exception/
    │                   │   │   └── BusinessRuleException.java
    │                   │   ├── model/                        (Entités métier)
    │                   │   │   ├── LearningPath.java
    │                   │   │   ├── PathAssignment.java
    │                   │   │   └── PathStatus.java
    │                   │   └── repository/                   (Interfaces Repository - Contrats)
    │                   │       ├── LearningPathRepository.java
    │                   │       └── PathAssignmentRepository.java
    │                   │
    │                   ├── application/                      (COUCHE APPLICATION - Dépend du domaine)
    │                   │   └── service/
    │                   │       └── PathAssignmentService.java
    │                   │
    │                   └── infrastructure/                   (COUCHE INFRASTRUCTURE - Dépend du domaine)
    │                       └── repository/
    │                           ├── InMemoryLearningPathRepository.java
    │                           └── InMemoryPathAssignmentRepository.java
    │
    └── test/
        └── java/
            └── com/
                └── learningplatform/
                    └── learning/
                        └── PathAssignmentServiceTest.java
```

## Exemple d'un service métier

Le service métier `PathAssignmentService` illustre l'utilisation exclusive des contrats (interfaces Repository) et des règles métier, sans connaissance des détails d'implémentation.

### Service PathAssignmentService

Le service `PathAssignmentService` ne doit connaître que :
- les règles métier
- l'interface `LearningPathRepository`
- l'interface `PathAssignmentRepository`

Il ne doit pas connaître :
- SQL
- ORM
- InMemory
- les détails de stockage

### Règle métier implémentée

"Un parcours ne peut pas être assigné à un élève s'il n'est pas validé."

### Implémentation du service

Le service `assignPathToStudent(UUID pathId, UUID studentId)` :

1. récupère le parcours via `learningPathRepository.findPathById(pathId)`
2. valide que le parcours existe (sinon lève une BusinessRuleException)
3. valide la règle métier : vérifie que le status du parcours est VALIDATED (sinon lève une BusinessRuleException)
4. crée l'assignation via `pathAssignmentRepository.assignPathToStudent(pathId, studentId)`

Le service utilise uniquement les méthodes définies dans les interfaces Repository. Il ignore complètement si le stockage est en mémoire (InMemory), en base de données (JPA), ou dans un autre système. Cette abstraction permet de changer l'implémentation technique sans modifier le service métier.

## Séparation des couches et inversion de dépendance

**Couche Domain (domaine) :**
- Contient les entités métier (LearningPath, PathAssignment, PathStatus)
- Contient les interfaces Repository (LearningPathRepository, PathAssignmentRepository)
- Contient les exceptions métier (BusinessRuleException)
- Ne dépend d'aucune autre couche
- Représente le cœur métier stable et indépendant

**Couche Application (application) :**
- Contient les services métier (PathAssignmentService)
- Dépend uniquement du domaine via les interfaces Repository
- Utilise les abstractions du domaine, pas les implémentations concrètes
- Orchestre les règles métier en utilisant les repositories

**Couche Infrastructure (infrastructure) :**
- Contient les implémentations concrètes des repositories (InMemoryLearningPathRepository, InMemoryPathAssignmentRepository)
- Dépend du domaine car elle implémente les interfaces définies dans le domaine
- Le domaine ne dépend pas de l'infrastructure, c'est l'inversion de dépendance
- Peut être remplacée sans modifier le domaine ou l'application (exemple : remplacer InMemory par JPA, MongoDB, etc.)

**Couche Test :**
- Contient les tests unitaires (PathAssignmentServiceTest)
- Utilise les implémentations InMemory pour tester sans infrastructure externe
- Démontre le découplage : les tests fonctionnent sans base de données ni ORM

### Flux de dépendance

Le flux de dépendance suit le principe d'inversion de dépendance :

```
Infrastructure → Domain ← Application
     ↓              ↑
     └──────────────┘
```

- Domain est indépendant (ne dépend de rien)
- Application dépend de Domain (via les interfaces)
- Infrastructure dépend de Domain (implémente les interfaces)
- Domain ne dépend jamais d'Infrastructure

Cette architecture permet de changer l'implémentation technique (InMemory, JPA, MongoDB, etc.) sans modifier le domaine ni les services métier.

