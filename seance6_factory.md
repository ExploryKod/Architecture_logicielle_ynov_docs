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

## Sommaire

1. [Synthèse](#synthèse)
2. [Contexte du projet](#contexte-du-projet)
3. [Factory globale](#factory-globale)
4. [Schéma de création des objets](#schéma-de-création-des-objets)
5. [Justification des choix](#justification-des-choix)
6. [Impact sur l'architecture](#impact-sur-larchitecture)
7. [Relier les choix aux objectifs pédagogiques](#relier-les-choix-aux-objectifs-pédagogiques)

## Synthèse

Ce livrable présente l'introduction d'une Factory globale associée au pattern Singleton pour centraliser la création des objets techniques (repositories) dans le contexte du module learning-module. L'objectif est de découpler le métier de la création de ses dépendances techniques, garantissant un point d'instanciation unique et facilitant l'évolution des implémentations (InMemory à JPA, MongoDB, etc.).

### Synthèse des choix architecturaux

**Factory** : Retenue sous forme d'une Factory globale (`RepositoryFactory`) pour centraliser tous les `new` des repositories techniques. Choix justifié par la simplicité, la centralisation et l'uniformité des règles de création à ce stade du projet.

**Singleton** : Retenu pour garantir un point d'accès unique à la Factory. Périmètre limité à l'encapsulation de la Factory uniquement. Limites identifiées : état partagé entre tests, remplaçable par injection de dépendance en production.

**Builder** : Volontairement non retenu. Justification : complexité inutile pour des repositories sans configuration complexe. La création directe via `new` est suffisante et lisible dans ce contexte.

**Objectifs pédagogiques atteints** :
- Structuration de l'instanciation : tous les `new` centralisés dans la Factory
- Découplage métier/technique : le métier ne connaît plus les classes concrètes
- Justification claire : chaque choix est argumenté et relié aux besoins du projet

## Contexte du projet

Le projet est une plateforme d'apprentissage SAAS modulable organisée en architecture hexagonale avec séparation des couches Domain, Application et Infrastructure. Le module learning-module utilise actuellement des repositories InMemory créés directement dans les tests (voir `PathAssignmentServiceTest`). La Factory permettra de centraliser cette création et de faciliter l'évolution vers d'autres implémentations techniques.

## Factory globale

### Concept

La Factory globale `RepositoryFactory` constitue le point d'instanciation centralisé et lisible pour tous les objets techniques. Tous les `new` des repositories techniques sont regroupés dans cette Factory unique, accessible via un Singleton garantissant un point d'accès unique à travers l'application.

### Choix d'une Factory globale plutôt que des factories par domaine

**Choix retenu** : Une seule Factory globale (`RepositoryFactory`) plutôt que des factories par domaine (LearningFactory, UserFactory, BillingFactory, etc.).

**Justification détaillée** :

**Raisons principales** :
1. **Simplicité** : À ce stade du projet, une Factory globale est la solution la plus simple et la plus cohérente. Pas de complexité inutile.
2. **Centralisation maximale** : Tous les `new` des objets techniques sont regroupés dans un seul endroit, facilement identifiable et modifiable. Un seul fichier à consulter pour comprendre comment les repositories sont créés.
3. **Lisibilité** : Le point d'instanciation est unique et évident, sans dispersion dans plusieurs factories. Un développeur sait immédiatement où chercher.
4. **Cohérence des règles** : Les règles de création des repositories techniques sont uniformes (tous InMemory actuellement, tous JPA demain). Pas de variation par domaine.
5. **Pas de justification pour la séparation** : Créer des factories par domaine nécessiterait une justification claire (ex : règles de création très différentes par domaine), ce qui n'est pas le cas ici.

**Cas où des factories par domaine seraient justifiées** :
- Si chaque domaine avait des règles de création très spécifiques et différentes (ex : LearningFactory nécessite un cache distribué, BillingFactory nécessite une connexion sécurisée spécifique avec certificats)
- Si les dépendances techniques variaient significativement par domaine (ex : Learning utilise MongoDB, Billing utilise PostgreSQL avec des règles de connexion différentes)
- Si la complexité de création justifiait la séparation (ex : LearningFactory avec 10 paramètres de configuration, BillingFactory avec 5 autres paramètres)

**Dans notre contexte** : Tous les repositories suivent le même pattern de création simple (`new InMemoryXxxRepository()`). Aucune raison de séparer.

**Décision** : Factory globale retenue car elle répond aux objectifs pédagogiques (centralisation, simplicité, lisibilité) sans complexité inutile.

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
- Point d'accès unique : garantit qu'une seule instance de Factory existe dans toute l'application
- Cohérence : tous les composants utilisent la même Factory, donc les mêmes règles de création
- Simplicité : accès direct via `getInstance()` sans gestion de dépendances complexes ni conteneur d'injection
- Centralisation : facilite la maintenance et l'évolution des règles de création
- Objectif pédagogique : comprendre le principe de centralisation sans la complexité d'un framework d'injection de dépendance

**Périmètre d'utilisation** :
- Le Singleton encapsule uniquement la Factory (`RepositoryFactory`)
- Il ne crée jamais d'objets métier (LearningPath, PathAssignment, etc.)
- Il ne gère pas d'état métier, seulement l'instance unique de la Factory
- Utilisé uniquement pour accéder à la Factory, pas pour stocker des données applicatives

**Limites identifiées** :
- **État partagé entre tests** : L'instance unique peut causer des interférences entre tests unitaires si l'état de la Factory est modifié
- **Testabilité réduite** : Difficile de remplacer la Factory par un mock dans les tests sans modifier le code
- **Pas thread-safe** : L'implémentation basique présentée n'est pas thread-safe (problème si accès concurrent)
- **Couplage global** : Tous les composants dépendent directement de la Factory via `getInstance()`, créant un couplage global

**Quand ne pas utiliser le Singleton** :
- En production avec un framework d'injection de dépendance (Spring, Guice) : préférer l'injection de la Factory
- Si plusieurs configurations de Factory sont nécessaires (ex : Factory pour tests, Factory pour production)
- Si la Factory doit être remplaçable par des mocks dans les tests

**Alternative en production** : Remplacer le Singleton par une injection de dépendance tout en conservant le principe de Factory. La Factory devient un composant géré par le conteneur, injecté là où nécessaire.

**Anti-pattern évité** : Singleton qui crée du métier. Le Singleton encapsule uniquement la Factory, qui crée des objets techniques. Aucune logique métier n'est encapsulée dans le Singleton.

### Builder non retenu

**Choix** : Builder volontairement non utilisé.

**Justification du non-usage** :
- **Complexité inutile** : Les repositories InMemory n'ont pas de configuration complexe nécessitant un Builder. Ils se créent sans paramètres.
- **Simplicité suffisante** : La création directe via `new` dans la Factory est claire, lisible et suffisante pour ce contexte.
- **Pas de paramètres optionnels** : Chaque repository se crée sans configuration (pas de connexion, pas de pool, pas de timeout, pas de stratégie de cache).
- **Pas de construction étape par étape** : La création est atomique, pas besoin de construire l'objet progressivement.
- **Principe KISS (Keep It Simple, Stupid)** : Ajouter un Builder ajouterait de la complexité sans bénéfice dans ce contexte.

**Quand le Builder serait justifié** :
- Si les repositories nécessitent une configuration complexe avec de nombreux paramètres optionnels
- Si la création nécessite une construction étape par étape (ex : configurer connexion, puis pool, puis cache)
- Si plusieurs variantes de repositories doivent être créées avec des configurations différentes
- Si la lisibilité du code de création serait améliorée (ex : `RepositoryBuilder.new().withConnection(...).withCache(...).build()`)

**Exemple où le Builder serait pertinent** :
```java
// Si on avait besoin de ça, le Builder serait justifié :
JPARepositoryBuilder.new()
    .withEntityManager(entityManager)
    .withCacheStrategy(CacheStrategy.LRU)
    .withConnectionPool(poolSize: 10)
    .withTimeout(duration: 30s)
    .build();
```

**Dans notre cas** : La Factory suffit car `new InMemoryLearningPathRepository()` est simple et lisible. Le Builder serait une sur-ingénierie.

**Décision** : Pattern Builder volontairement écarté car non nécessaire dans ce contexte. La Factory avec création directe est la solution la plus simple et la plus adaptée.

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

## Relier les choix aux objectifs pédagogiques

### Objectif 1 : Structurer l'instanciation

**Réalisation** :
- Tous les `new` des repositories techniques sont centralisés dans la Factory globale
- Le point d'instanciation est unique, identifié et lisible
- Aucun `new` de repository technique n'apparaît dans le métier ou les tests

**Justification du choix** : Factory globale plutôt que factories par domaine car la centralisation maximale est plus importante que la séparation par domaine à ce stade.

### Objectif 2 : Découpler le métier de la technique

**Réalisation** :
- Le métier (`PathAssignmentService`) ne connaît plus les classes concrètes (InMemory*, JPA*)
- Le métier dépend uniquement des interfaces du domaine (`LearningPathRepository`, `PathAssignmentRepository`)
- Un changement technique (InMemory à JPA) ne nécessite qu'une modification dans la Factory

**Justification du choix** : La Factory crée les implémentations techniques, le métier utilise les abstractions. Le Singleton garantit l'accès unique mais n'interfère pas avec le découplage.

### Objectif 3 : Justifier les choix de manière claire et cohérente

**Réalisation** :
- **Factory** : Retenue car nécessaire pour centraliser les `new` et découpler le métier de la technique
- **Singleton** : Retenu pour simplifier l'accès à la Factory, avec limites identifiées (état partagé, testabilité)
- **Builder** : Volontairement écarté car complexité inutile pour des repositories sans configuration

**Cohérence** : Chaque choix est argumenté en fonction du contexte du projet (simplicité, centralisation, découplage) et non appliqué mécaniquement.
