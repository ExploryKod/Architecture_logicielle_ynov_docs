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

### Interfaces : Repository.

Dans notre cas, nos n'implémentons pas de DAO. A ce stade de notre projet nous n'en avons pas besoin car nous n'allons pas dans une analyse trés Macro. 

Les Repository appartiennent strictement au domaine et permettent d'abstraire l'accès aux données. Il exprime des règles métier même si les termes peuvent être simple comme "save". Ils ne dépendent pas d'un ORM, ne dépendent pas d'une base de données, d'une technologie ou autre infrastructure.

Ils sont donc appellé par nos services métiers qui ne connaissant pas l'implémentation concrète. L'infrastructure fourni une implémentation technique de ces interfaces mais elle pourrait changer sans que les interfaces ne doivent elles changer. Les services n'ont pas non plus besoin de changer si l'implémentation technique change. C'est tout l'intérêt de cette couche d'abstraction qui permet l'inversion de dépendance entre infrastructure et domaine. Le domaine ne dépend pas de l'infrastructure mais l'infrastructure dépend du domaine.

#### Interface `LearningPathRepository`
Responsable de la gestion des parcours d'apprentissage.

**Méthodes orientées métier :**

- savePath(LearningPath path) - sauvegarde un parcours métier
- findPathById(String pathId) - récupère un parcours métier
- findValidatedPaths() - liste les parcours prêts pour assignation
- findDraftsByTeacher(String teacherId) - trouve parcours en construction d'un professeur (brouillon)
- archivePath(String pathId) - archive un parcours 

Retourne des objets métier purs (LearningPath du domaine).
Le nom des méthodes reflète l'intention métier, pas l'opération technique.

Exemple en Java :

Pour la lisibilité nous utilisons un exemple en Java mais cela peut s'écrire dans n'importe quel langage orienté objet.

```java
public interface LearningPathRepository {
    void savePath(LearningPath path);
    LearningPath findById(String pathId);
    List<LearningPath> findValidatedPaths();
    List<LearningPath> findDraftsByTeacher(String teacherId);
    void archivePath(String pathId);
}
```