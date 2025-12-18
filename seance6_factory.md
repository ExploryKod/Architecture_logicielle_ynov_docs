# Renforcer le couplage faible via des design pattern ciblé

### Exercice de la séance 6 : 
L'objectif est d'avoir un point unique responsable de la création des objets.
Le design pattern le plus approprié pour cela est la factory. Le Singleton viendra aussi renforcer cette objectif. Le builder sera présent si besoin.

À la fin de la séance, vous devez être capables de présenter :
- une Factory globale (même conceptuelle),
- un schéma ou pseudo-code montrant qui crée quoi,
-​ une justification écrite de vos choix (Factory, Singleton, Builder ou non).
Il n'est pas attendu de coder une application complète.

Après avoir découplé le métier du stockage, vous découplez maintenant le
métier de la création des objets.

Un travail est réussi si le point d'instanciation est unique, le métier ne crée plus ses dépendances, un changement technique passe par la Factory, les choix sont argumentés.

Attention : la factory ou le singleton ne doit pas créé du métier.

## Livrable Séance 6 - Factory et Singleton

## Synthèse

Ce livrable présente l'introduction d'une Factory globale associée au pattern Singleton pour centraliser la création des objets techniques (repositories) dans le contexte du module learning-module. L'objectif est de découpler le métier de la création de ses dépendances techniques, garantissant un point d'instanciation unique et facilitant l'évolution des implémentations (InMemory à JPA, MongoDB, etc.).

## Contexte du projet

Le projet est une plateforme d'apprentissage SAAS modulable organisée en architecture hexagonale avec séparation des couches Domain, Application et Infrastructure. Le module learning-module utilise actuellement des repositories InMemory créés directement dans les tests (voir `PathAssignmentServiceTest`). La Factory permettra de centraliser cette création et de faciliter l'évolution vers d'autres implémentations techniques.

## Factory globale

### Concept

La Factory globale `RepositoryFactory` constitue le point d'instanciation centralisé et lisible pour tous les objets techniques. Tous les `new` des repositories techniques sont regroupés dans cette Factory unique, accessible via un Singleton garantissant un point d'accès unique à travers l'application.

### Choix d'une Factory globale plutôt que des factories par domaine

**Choix retenu** : Une seule Factory globale (`RepositoryFactory`) plutôt que des factories par domaine (LearningFactory, UserFactory, BillingFactory, etc.).

**Justification** :
- Simplicité : à ce stade du projet, une Factory globale est la solution la plus simple et la plus cohérente
- Centralisation : tous les `new` des objets techniques sont regroupés dans un seul endroit, facilement identifiable et modifiable
- Lisibilité : le point d'instanciation est unique et évident, sans dispersion dans plusieurs factories
- Cohérence : les règles de création des repositories techniques sont uniformes (tous InMemory actuellement, tous JPA demain)
- Pas de complexité inutile : créer des factories par domaine nécessiterait une justification claire (ex : règles de création très différentes par domaine), ce qui n'est pas le cas ici

**Cas où des factories par domaine seraient justifiées** : Si chaque domaine avait des règles de création très spécifiques (ex : LearningFactory nécessite un cache distribué, BillingFactory nécessite une connexion sécurisée spécifique), alors des factories séparées seraient justifiées. Ce n'est pas le cas ici où tous les repositories suivent le même pattern de création.

### Responsabilités

La Factory crée exclusivement des objets techniques :
- Implémentations de repositories (InMemoryLearningPathRepository, InMemoryPathAssignmentRepository)
- Futures implémentations techniques (JPALearningPathRepository, MongoDBLearningPathRepository, etc.)

La Factory ne crée jamais d'objets métier (LearningPath, PathAssignment, Student, Teacher). Ces entités métier sont créées par le domaine lui-même selon ses règles métier.

**Point d'instanciation centralisé** : Tous les `new` des repositories techniques sont regroupés dans cette Factory. Aucun `new` de repository technique ne doit apparaître ailleurs dans le code (tests, application, infrastructure).

### Structure conceptuelle

```
RepositoryFactory (Singleton)
├── createLearningPathRepository() retourne LearningPathRepository
├── createPathAssignmentRepository() retourne PathAssignmentRepository
└── createUserRepository() retourne UserRepository (futur)
```

## Schéma de création des objets

### Situation actuelle (avant Factory)

Dans le test `PathAssignmentServiceTest`, les repositories sont créés directement :

```java
@BeforeEach
void setUp() {
    learningPathRepository = new InMemoryLearningPathRepository();
    pathAssignmentRepository = new InMemoryPathAssignmentRepository();
    pathAssignmentService = new PathAssignmentService(
        learningPathRepository,
        pathAssignmentRepository
    );
}
```

**Flux actuel** :
```
Test/Application
    new InMemoryLearningPathRepository()
    new InMemoryPathAssignmentRepository()
Infrastructure (InMemory*)
    injection via constructeur
Application (PathAssignmentService)
```

**Problème** : Les instances techniques sont créées directement dans les tests ou l'application. Un changement d'implémentation (InMemory à JPA) nécessite de modifier tous les points de création.

### Solution avec Factory

**Flux avec Factory** :
```
Test/Application
    RepositoryFactory.getInstance()
    .createLearningPathRepository()
    .createPathAssignmentRepository()
RepositoryFactory (Singleton)
    new InMemoryLearningPathRepository()
    new InMemoryPathAssignmentRepository()
Infrastructure (InMemory*)
    injection via constructeur
Application (PathAssignmentService)
```

**Avantage** : Un seul point de création. Un changement d'implémentation (InMemory à JPA) ne nécessite qu'une modification dans la Factory.

### Pseudo-code

#### Factory avec Singleton

```java
public class RepositoryFactory {
    private static RepositoryFactory instance;
    
    private RepositoryFactory() {}
    
    public static RepositoryFactory getInstance() {
        if (instance == null) {
            instance = new RepositoryFactory();
        }
        return instance;
    }
    
    public LearningPathRepository createLearningPathRepository() {
        return new InMemoryLearningPathRepository();
    }
    
    public PathAssignmentRepository createPathAssignmentRepository() {
        return new InMemoryPathAssignmentRepository();
    }
}
```

#### Utilisation dans les tests

```java
@BeforeEach
void setUp() {
    RepositoryFactory factory = RepositoryFactory.getInstance();
    
    LearningPathRepository learningPathRepository = 
        factory.createLearningPathRepository();
    PathAssignmentRepository pathAssignmentRepository = 
        factory.createPathAssignmentRepository();
    
    pathAssignmentService = new PathAssignmentService(
        learningPathRepository,
        pathAssignmentRepository
    );
}
```

#### Utilisation dans l'application

```java
public class ApplicationBootstrap {
    public void initialize() {
        RepositoryFactory factory = RepositoryFactory.getInstance();
        
        LearningPathRepository learningPathRepo = 
            factory.createLearningPathRepository();
        PathAssignmentRepository assignmentRepo = 
            factory.createPathAssignmentRepository();
        
        PathAssignmentService service = new PathAssignmentService(
            learningPathRepo,
            assignmentRepo
        );
    }
}
```

### Changement d'implémentation technique

Pour passer de InMemory à JPA, seule la Factory est modifiée :

```java
public LearningPathRepository createLearningPathRepository() {
    // Avant : return new InMemoryLearningPathRepository();
    return new JPALearningPathRepository(entityManager);
}
```

Le métier (`PathAssignmentService`) et les tests restent inchangés car ils dépendent uniquement des interfaces du domaine (`LearningPathRepository`, `PathAssignmentRepository`).

## Justification des choix

### Factory

**Choix retenu** : Factory globale pour centraliser la création des repositories techniques.

**Justification** :
- Point d'instanciation centralisé et lisible : tous les `new` des repositories techniques sont regroupés dans un seul endroit, facilement identifiable
- Point unique de création : tous les repositories techniques sont créés au même endroit, évitant la dispersion des `new` dans le code
- Découplage métier/création : le métier ne connaît plus les classes concrètes (InMemory*, JPA*)
- Facilité d'évolution : changement d'implémentation technique isolé dans la Factory, tous les `new` sont modifiés au même endroit
- Respect de l'inversion de dépendance : le métier dépend des interfaces, la Factory crée les implémentations
- Cohérence avec l'architecture hexagonale : la Factory appartient à l'infrastructure et crée les adapters techniques
- Simplicité : une Factory globale est la solution la plus simple et la plus cohérente à ce stade, sans complexité inutile

**Anti-pattern évité** : Factory qui crée du métier. La Factory crée uniquement des objets techniques (repositories), jamais des entités métier (LearningPath, PathAssignment). Les entités métier sont créées par le domaine selon ses règles métier.

### Singleton

**Choix retenu** : Singleton pour garantir un point d'accès unique à la Factory.

**Justification** :
- Point d'accès unique : garantit qu'une seule instance de Factory existe
- Cohérence : tous les composants utilisent la même Factory, donc les mêmes règles de création
- Simplicité : accès direct via `getInstance()` sans gestion de dépendances complexes
- Centralisation : facilite la maintenance et l'évolution des règles de création

**Anti-pattern évité** : Singleton qui crée du métier. Le Singleton encapsule uniquement la Factory, qui crée des objets techniques.

**Limitation acceptée** : Le Singleton rend les tests unitaires plus difficiles (état partagé entre tests). Pour un usage en production, une injection de dépendance (Spring, Guice) serait préférable, mais dépasse le cadre de cet exercice qui vise à comprendre le principe de centralisation.

### Builder non retenu

**Choix** : Builder non utilisé.

**Justification** :
- Complexité inutile : les repositories n'ont pas de configuration complexe nécessitant un Builder
- Simplicité suffisante : la création directe via `new` est claire et lisible pour les repositories InMemory
- Pas de paramètres optionnels multiples : chaque repository se crée sans configuration complexe
- Les repositories InMemory n'ont pas besoin de configuration (pas de connexion, pas de pool, etc.)

**Cas d'usage futur** : Un Builder pourrait être introduit si la création de repositories nécessite une configuration complexe (pool de connexions, stratégies de cache, configuration de timeout, etc.). Pour l'instant, la Factory suffit.

## Impact sur l'architecture

### Découplage renforcé

**Avant** :
- Les `new` des repositories étaient dispersés dans les tests et l'application
- Le métier recevait ses dépendances mais leur création était dispersée
- Un changement technique (InMemory à JPA) nécessitait de modifier tous les points de création (tous les `new`)
- Le couplage avec les classes concrètes (InMemory*) était présent dans les tests

**Après** :
- Tous les `new` des repositories techniques sont centralisés dans la Factory globale
- Le point d'instanciation est unique, centralisé et lisible
- Le métier reçoit ses dépendances sans connaître leur création
- Un changement technique ne nécessite qu'une modification dans la Factory (tous les `new` sont au même endroit)
- Les tests et l'application ne connaissent plus les classes concrètes, seulement la Factory

### Respect des principes SOLID

- **Single Responsibility** : La Factory a une seule responsabilité (créer des repositories techniques)
- **Dependency Inversion** : Le métier dépend des abstractions (interfaces Repository), la Factory crée les implémentations
- **Open/Closed** : Ouvert à l'extension (nouveaux types de repositories), fermé à la modification du métier

### Cohérence avec l'architecture hexagonale

La Factory s'intègre naturellement dans l'architecture hexagonale :
- **Domain** : définit les interfaces Repository (LearningPathRepository, PathAssignmentRepository)
- **Infrastructure** : contient les implémentations (InMemory*, JPA*, etc.) et la Factory qui les crée
- **Application** : utilise les interfaces sans connaître leur création

Le flux de dépendance reste respecté : Infrastructure dépend de Domain, Application dépend de Domain

### Limites et évolutions

**Limite actuelle** : Le Singleton rend les tests plus complexes (état partagé entre tests). Pour isoler les tests, il faudrait réinitialiser l'instance ou utiliser une approche différente.

**Évolution future** : Remplacer le Singleton par une injection de dépendance (Spring, Guice) pour une meilleure testabilité tout en conservant le principe de Factory. La Factory pourrait devenir un composant géré par le conteneur d'injection de dépendance.
