# Industrialisation du projet - Structure de build et versioning

## Sommaire

1. [Synthèse](#synthèse)
2. [Arborescence logique du projet](#arborescence-logique-du-projet)
3. [Découpage en modules figé](#découpage-en-modules-figé)
4. [Règles de versioning définies](#règles-de-versioning-définies)
5. [Déploiement sur VPS avec Docker](#déploiement-sur-vps-avec-docker)

## Introduction

Ce livrable définit la structure de build et de versioning du projet Learning Platform pour préparer son industrialisation. L'objectif est de figer l'organisation en modules Maven, établir une arborescence logique cohérente et définir des règles de versioning claires pour faciliter la maintenance, l'évolution et le déploiement.

Pour répondre à l'exercice nous nous positionnons sur du Java avec Maven mais les autres options technologiques seront mis en place pour le fil rouge RNCP (Pour le MVC).

Hébergeur : nous choisissons Hostinger qui propose de bon VPS afin d'héberger avec le maximum de flexibilité pour un coût raisonnable et une courbe d'apprentissage aisé car je maîtrise déjà la dockerisation. AWS n'est pas intéressant pour la phase MVC car ce dernier n'est pas concerné par les risques de montée en charge. Il fut néanmoins prévoir une migration.

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

Nos branches : 

Le projet utilise le versioning sémantique (Semantic Versioning) avec le format `MAJOR.MINOR.PATCH-SUFFIX`.

- **major** : Changements incompatibles avec les versions précédentes (changements d'API, suppression de fonctionnalités)
- **minor** : Ajout de fonctionnalités rétrocompatibles (nouvelles fonctionnalités, nouvelles interfaces)
- **patch** : Corrections de bugs rétrocompatibles (fixes, améliorations mineures)
- **suffix** : Indicateur de stabilité (`SNAPSHOT` pour développement, absence pour release)
- **experimental** : si on a des modules expérimentales à tester qui ne seront jamais mergés mais dont on veut garder trace.

Branche de travail commun : on ne fait que merger sur ces branches.
- **develop** : branche de travail tant que l'on a pas mis en production (miroir du travail en phase dev)
- **main** : branche de travail miroir du serveur de production (on ne merge ici que ce qui ira en production).
- **release** : branche si une fois le projet en production, l'on décide de travailer sur une nouvelle version de l'application (cf Version plus : cela implique de bien changer la version du projet partout où il est mentionné).
- **refactor** : branche dédié au refactoring donc aucune nouvelle fonctionnalité sera mise en place

**Develop** : branche qu'on abandonne quand on a finit le travail de développement et donc on ne fait plus partir de branche de cette branche **develop** à ce stade. 
Master sera alors la branche où chacun merge des **minor**, **major**, **patch**



Nos commits : conventionnal commit. 

- Pour le travail sur une fonctionnalité :
  *feat(nom-dossier): description concise, courte et précise (ajout optionnel de --wip si le travail est en cours)*

- Pour le travail sur les bugs : 
  *doc(nom-du-doc): description de la tâche effectuée, courte et précise*

- Pour le travail de refactoring : 
   *refactor(nom-du-fichier): description précise et courte de l'opération de refactoring*

- Pour la documentation du projet : *docs: description de l'ahjout du doc*

### Version actuelle de l'application

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

## Déploiement sur VPS avec Docker

### Stratégie de déploiement

Le déploiement sur un VPS Hostinger utilise Docker et Docker Compose pour isoler l'environnement d'exécution et simplifier la gestion des dépendances. Java n'est pas installé directement sur le VPS : il est fourni par les images Docker officielles.

### Avantages de l'approche Docker

**Isolation** : Chaque service s'exécute dans son propre conteneur, évitant les conflits de dépendances.

**Portabilité** : L'application fonctionne de manière identique en développement et en production.

**Simplicité** : Pas besoin d'installer Java, Maven ou PostgreSQL directement sur le VPS. Tout est contenu dans les images Docker.

**Maintenance** : Mise à jour facilitée via le changement d'image Docker.

### Architecture de déploiement

```
VPS Hostinger
├── Docker Engine
├── Docker Compose
└── Conteneurs
    ├── api-gateway (Spring Boot + Java 17)
    ├── postgresql (Base de données)
    └── nginx (Reverse proxy optionnel)
```

### Configuration Docker

#### Dockerfile multi-stage

Chaque module Spring Boot utilise un Dockerfile multi-stage pour optimiser la taille de l'image :

```dockerfile
# Stage 1 : Build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY . .
RUN mvn clean package -DskipTests

# Stage 2 : Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Avantages** : Image finale légère (seulement le JAR et le JRE), pas de Maven dans l'image de production.

#### Docker Compose

Le fichier `docker-compose.yml` orchestre tous les services :

```yaml

services:
  postgresql:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: learning_platform
      POSTGRES_USER: ${DB_USER}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DB_USER}"]
      interval: 10s
      timeout: 5s
      retries: 5

  api-gateway:
    build:
      context: ./api-gateway
      dockerfile: Dockerfile
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgresql:5432/learning_platform
      SPRING_DATASOURCE_USERNAME: ${DB_USER}
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      SPRING_PROFILES_ACTIVE: production
    ports:
      - "8080:8080"
    depends_on:
      postgresql:
        condition: service_healthy
    restart: unless-stopped

volumes:
  postgres_data:
```

### Installation sur VPS Hostinger

#### Prérequis

**Installation de Docker** : Le VPS doit avoir Docker et Docker Compose installés. Hostinger fournit généralement un VPS avec accès root permettant l'installation.

**Commandes d'installation** :
```bash
# Mise à jour du système
sudo apt update && sudo apt upgrade -y

# Installation de Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Installation de Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Vérification
docker --version
docker-compose --version
```

**Java via Docker** : Java n'est pas installé directement sur le VPS. Les images Docker `eclipse-temurin:17-jre-alpine` contiennent Java 17. Aucune installation Java native n'est nécessaire.

#### Déploiement

**Structure des fichiers sur le VPS** :
```
learning-platform/
├── docker-compose.yml
├── .env
├── api-gateway/
│   └── Dockerfile
└── [autres modules]
```

**Variables d'environnement** (fichier `.env`) :
```
DB_USER=learning_user
DB_PASSWORD=secure_password
SPRING_PROFILES_ACTIVE=production
```

**Commandes de déploiement** :
```bash
# Cloner ou transférer le projet
Aller dans le dossier du projet
cd (..)/learning-platform

# Construire et démarrer les services
docker-compose build
docker-compose up -d

# Vérifier les logs
docker-compose logs -f api-gateway

# Vérifier le statut
docker-compose ps
```

### Gestion de la production

**Mise à jour** : Pour déployer une nouvelle version, reconstruire les images et redémarrer les conteneurs :
```bash
docker-compose build --no-cache
docker-compose up -d
```

**Sauvegarde de la base de données** :
```bash
docker-compose exec postgresql pg_dump -U learning_user learning_platform > backup.sql
```

**Monitoring** : Utiliser `docker-compose logs` et `docker stats` pour surveiller les performances.

**Sécurité** : Configurer un firewall (UFW) pour n'exposer que les ports nécessaires (80, 443, 22).
Hostinger propose un firewall mise en place sur mon VPS que je peux activer depuis une interface : c'est plus simple de gérer le firewall de cette façon car il sera toujours accessible (pas besoin de l'accés ssh).

### Avantages de cette approche

**Pas d'installation Java native** : Java est fourni par l'image Docker, simplifiant la maintenance et évitant les conflits de versions.

**Isolation complète** : Chaque service est isolé, facilitant le debugging et la mise à jour.

**Reproductibilité** : L'environnement de production est identique à celui de développement.

**Scalabilité** : Facilite l'ajout de nouveaux services ou la montée en charge via Docker Swarm ou Kubernetes.
Cela permet aussi une migration plus aisé pour passer du MVC à l'application fini sur AWS.