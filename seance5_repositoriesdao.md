# Livrable Séance 5 - Repositories/DAO

## Consignes (rappel)

### À la fin du livrable, on doit retrouver dans ton projet :
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