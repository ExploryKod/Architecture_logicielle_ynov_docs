# Industrialisation du projet - Structure de build et versioning

## Sommaire

1. [Synthèse](#synthèse)
2. [Arborescence logique du projet](#arborescence-logique-du-projet)
3. [Découpage en modules figé](#découpage-en-modules-figé)
4. [Règles de versioning définies](#règles-de-versioning-définies)

## Synthèse

Ce livrable définit la structure de build et de versioning du projet Learning Platform pour préparer son industrialisation. L'objectif est de figer l'organisation en modules Maven, établir une arborescence logique cohérente et définir des règles de versioning claires pour faciliter la maintenance, l'évolution et le déploiement.

## Arborescence logique du projet

### Structure globale

Le projet suit une architecture multi-modules Maven organisée selon le principe d'architecture hexagonale avec séparation Domain/Application/Infrastructure.

```
learning-platform/
├── pom.xml (POM parent)
├── core/
│   └── pom.xml
├── user-module/
│   └── pom.xml
├── learning-module/
│   └── pom.xml
├── ai-module/
│   └── pom.xml
├── communication-module/
│   └── pom.xml
├── billing-module/
│   └── pom.xml
├── reporting-module/
│   └── pom.xml
└── api-gateway/
    └── pom.xml
```

### Organisation interne des modules

Chaque module métier suit la même structure logique en trois couches :

```
module-name/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/learningplatform/module-name/
    │   │       ├── domain/          (Couche domaine - ne dépend de rien)
    │   │       │   ├── model/       (Entités métier)
    │   │       │   ├── repository/  (Interfaces Repository)
    │   │       │   └── exception/   (Exceptions métier)
    │   │       ├── application/     (Couche application - dépend du domaine)
    │   │       │   └── service/      (Services métier)
    │   │       └── infrastructure/   (Couche infrastructure - dépend du domaine)
    │   │           ├── repository/  (Implémentations Repository)
    │   │           └── factory/      (Factories techniques)
    │   └── resources/
    │       └── application.yml
    └── test/
        └── java/
            └── com/learningplatform/module-name/
                └── [Tests unitaires et d'intégration]
```

### Hiérarchie des dépendances

Le module `core` constitue la base commune. Tous les modules métier dépendent de `core`. Les modules métier peuvent dépendre les uns des autres selon les besoins fonctionnels.

```
core (base commune)
    ↑
    ├── user-module
    ├── learning-module (dépend aussi de user-module)
    ├── ai-module
    ├── communication-module
    ├── billing-module
    └── reporting-module
```

## Découpage en modules figé

### Module core

**Artifact** : `com.learningplatform:core`

**Responsabilité** : Abstractions communes et interfaces partagées entre tous les modules.

**Contenu** :
- Exceptions communes (DomainException, ValidationException, etc.)
- Value objects partagés (EntityId, Email, TenantId, Timestamp)
- Interfaces de base (Repository, EventPublisher, ExternalService)
- Entités de base (BaseEntity, AuditableEntity)
- Contexte de sécurité (SecurityContext, TenantContext)

**Dépendances** : Aucune dépendance vers d'autres modules internes.

### Module user-module

**Artifact** : `com.learningplatform:user-module`

**Responsabilité** : Gestion des utilisateurs, écoles, rôles et permissions.

**Contenu** :
- Domain : User, School, Role, Permission, StudentProfile
- Application : Services d'authentification, gestion utilisateurs
- Infrastructure : Repositories, adapters REST, sécurité

**Dépendances** : `core`

### Module learning-module

**Artifact** : `com.learningplatform:learning-module`

**Responsabilité** : Parcours pédagogiques, outils de mémorisation, suivi de progression.

**Contenu** :
- Domain : LearningPath, PathAssignment, PathStatus, LearningStep
- Application : Services d'assignation, validation de parcours
- Infrastructure : Repositories InMemory/JPA, Factory

**Dépendances** : `core`, `user-module`

### Module ai-module

**Artifact** : `com.learningplatform:ai-module`

**Responsabilité** : Agents IA, RAG, intégration avec modèles de langage.

**Contenu** :
- Domain : Agent, DocumentSource, Prompt, Conversation
- Application : Services de configuration d'agents, requêtes IA
- Infrastructure : Adapters LLM (OpenAI, Ollama, Langchain)

**Dépendances** : `core`

### Module communication-module

**Artifact** : `com.learningplatform:communication-module`

**Responsabilité** : Messagerie, visioconférence, notifications.

**Contenu** :
- Domain : Message, Conversation, VideoSession, Notification
- Application : Services de messagerie, gestion de sessions
- Infrastructure : Adapters WebSocket, REST, email

**Dépendances** : `core`

### Module billing-module

**Artifact** : `com.learningplatform:billing-module`

**Responsabilité** : Abonnements, facturation, paiements.

**Contenu** :
- Domain : Subscription, Invoice, Payment, Contract
- Application : Services de facturation, gestion d'abonnements
- Infrastructure : Adapters passerelles de paiement, stockage de documents

**Dépendances** : `core`

### Module reporting-module

**Artifact** : `com.learningplatform:reporting-module`

**Responsabilité** : Tableaux de bord, rapports, statistiques.

**Contenu** :
- Domain : Dashboard, Report, Metric
- Application : Services d'agrégation, génération de rapports
- Infrastructure : Adapters d'export (PDF, CSV), agrégation de données

**Dépendances** : `core`, `user-module`, `learning-module`

### Module api-gateway

**Artifact** : `com.learningplatform:api-gateway`

**Responsabilité** : Point d'entrée unique, routage, sécurité, rate limiting.

**Contenu** :
- Configuration de sécurité (JWT, CORS)
- Filtres (Rate limiting, Tenant, Logging)
- Routage vers les modules métier

**Dépendances** : Tous les modules métier selon les besoins de routage

## Règles de versioning définies

### Stratégie de versioning

Le projet utilise le versioning sémantique (Semantic Versioning) avec le format `MAJOR.MINOR.PATCH-SUFFIX`.

**Format** : `X.Y.Z-SUFFIX`

- **MAJOR (X)** : Changements incompatibles avec les versions précédentes (changements d'API, suppression de fonctionnalités)
- **MINOR (Y)** : Ajout de fonctionnalités rétrocompatibles (nouvelles fonctionnalités, nouvelles interfaces)
- **PATCH (Z)** : Corrections de bugs rétrocompatibles (fixes, améliorations mineures)
- **SUFFIX** : Indicateur de stabilité (`SNAPSHOT` pour développement, absence pour release)

### Version actuelle

**Version de développement** : `1.0.0-SNAPSHOT`

Cette version indique que le projet est en phase de développement initial. Tous les modules partagent la même version via le POM parent.

### Règles de gestion des versions

**Version unifiée** : Tous les modules partagent la même version définie dans le POM parent (`learning-platform-parent`). Cette approche simplifie la gestion et garantit la cohérence.

**Incrémentation des versions** :
- **PATCH** : Correction de bugs, améliorations mineures sans changement d'API
- **MINOR** : Ajout de fonctionnalités, nouvelles interfaces Repository, nouveaux services
- **MAJOR** : Refactoring majeur, changement d'architecture, suppression d'API

**Gestion des dépendances inter-modules** : Les modules utilisent `${project.version}` pour référencer les autres modules internes, garantissant que tous les modules utilisent la même version.

### Cycle de release

**SNAPSHOT** : Version de développement, peut être modifiée à tout moment. Utilisée pendant le développement actif.

**Release** : Version stable publiée. Le suffixe `-SNAPSHOT` est retiré. Les releases sont immuables et ne peuvent pas être modifiées.

**Exemple de cycle** :
- Développement : `1.0.0-SNAPSHOT`
- Release candidate : `1.0.0-RC1`
- Release stable : `1.0.0`
- Développement suivant : `1.1.0-SNAPSHOT`

### Versioning des dépendances externes

Les versions des dépendances externes sont centralisées dans le POM parent via `dependencyManagement` :
- Spring Boot : `3.2.0`
- Lombok : `1.18.30`
- PostgreSQL : `42.7.1`
- JUnit : `5.10.1`

Les modules héritent de ces versions via le POM parent, garantissant la cohérence des dépendances.

### Règles de compatibilité

**Compatibilité ascendante** : Les nouvelles versions MINOR et PATCH doivent rester compatibles avec les versions précédentes de même MAJOR.

**Dépréciation** : Les fonctionnalités dépréciées sont marquées comme telles et supprimées uniquement lors d'un changement MAJOR.

**Migration** : Les changements MAJOR nécessitent un guide de migration documenté.
