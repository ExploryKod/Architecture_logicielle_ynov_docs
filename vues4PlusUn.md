# M2 Expert développeur web - Ynov

## Modèle 4+1 - Vue Logique 

Rappel du contexte du projet : 
L’entreprise fictive serait un fournisseur d’espaces au sein de cette plateforme d’apprentissage qui
agit comme un SAAS modulable. A l’image de Moodle Cloud qui fournit des espaces de “Learning
Management System” (LMS) afin que les écoles personnalisent les espaces d’échanges avec les
étudiants, nous fournissons des espaces qui permettent de créer des parcours d’apprentissage sur-
mesure avec des outils poussés de mémorisation personnalisable par élève, par matière et par
professeur.

Pour rappels nos blocs logiques sont les suivants :
- **Bloc 1 - Gestion des utilisateurs** : Inscription, connexion, gestion des profils, rôles et permissions.
- **Bloc 2 - Parcours et apprentissages** : Création et gestion des parcours pédagogiques, suivi des progrès, évaluations.
- **Bloc 3 - Agents IA et RAG** : Algorithmes pour générer des parcours d'apprentissage par IA, choix des modèles IA, outils RAG, prompts, recommandations
- **Bloc 4 - Communication et notifications** : Système de messagerie, chats entre les acteurs, notifications en temps réel.
- **Bloc 5 - Reporting et analyses** : Statistiques d'utilisation, rapports de performance, évolutions des résultats des élèves, états de complémtion des parcours, statistiques générales publique et d'autres sur accés professeur ou élève (authentifié) ...
- **Bloc 6 - Facturation et paiements (accés école)** : Gestion des abonnements, paiements en ligne à la plateforme d'apprentissage.

I/ Description des relations entre les blocs

### Utilisateurs vers Parcours

- Le professeur fournit son profil, ses classes, et configure les parcours.
- L’élève fournit son profil cognitif et son historique d’apprentissage pour personnalisation.

### Parcours vers utilisateurs
- Retour : progression de l’élève, tâches à réaliser, notifications.

### Parcours vers module de gestion des IA
- Transfert des sources documentaires sélectionnées par le professeur à l'IA.
- Transmission des objectifs pédagogiques, du niveau attendu et du type de réponses permis.
- Envoi de requêtes de génération de contenu ou traitement de questions posées par l’élève.

### Gestion IA vers Parcours
- Retour de contenus générés validables par le professeur.
- Retour de réponses conversationnelles archivables.
- Indicateurs sur la complexité des questions posées.

### Communication vers Utilisateurs
- Chat : messages entre professeur et élève, entre professeur et école, avec le support technique...
- Agenda : envoi de rappels entre élève et professeurs.
- Visioconférences : création de sessions liées aux utilisateurs.

### Utilisateurs vers Communication
- Informations d’identification pour sécuriser l’échange.
- Contexte du parcours pour contextualiser la conversation (ex : sur quelle matière).

### Reporting vers tous les blocs (sauf IA)
- Consomme les données issues des interactions, messages, activités, sessions IA, etc.

### Tous les blocs vers Reporting
- Données brutes : logs d’activité, progression, interactions, factures déposées, contenu généré.

### Facturation vers Utilisateurs
- Emission des factures pour chaque entité (école).
- Gestion des paiements, état, notifications.

### Utilisateurs vers Facturation
- Les écoles fournissent les informations de paiement, le niveau d’abonnement, les droits d’accès.

II/ Diagramme MCD (à venir)

**Relations principales entre entités :**
- Une École a plusieurs Professeurs.
- Une École possède plusieurs Élèves.
- Un Professeur gère plusieurs Classes.
- Une Classe regroupe plusieurs Élèves.
- Un Parcours est créé par un Professeur pour un Élève ou un groupe d’Élèves.
- Un Parcours est composé de plusieurs Étapes d’apprentissage.
- Chaque Étape référence des Contenus pédagogiques, validés ou importés.
- Chaque Élève réalise plusieurs Sessions de révision.
- Chaque Session de révision génère un Résultat (score, taux de réussite, temps passé).
- Un Agent IA appartient à une Discipline configurée par le Professeur.
- Un Agent IA utilise des Sources documentaires définies dans un Parcours.
- Un Agent IA enregistre des Interactions élève, archivées pour le Reporting.
- Une Conversation implique deux Utilisateurs (ex : professeur ↔ élève).
- Une Conversation contient plusieurs Messages.
- Une Facture est émise pour une École.
- Une Facture est associée à un Abonnement défini par la plateforme.

**Le Reporting agrège :**
- les Sessions de révision,
- les Interactions IA,
- les Conversations,
- les Résultats des élèves,
- les données de Facturation.

III/ Diagramme de Classes (à venir)

## Modèle 4+1 - Vue Développement

| **Module**               | **Classes clés**                                                                |
| ------------------------ | ------------------------------------------------------------------------------- |
| **module-users**         | User, StudentProfile, Role, Permission, UserService                             |
| **module-learning**      | LearningPath, LearningStep, RevisionSession, ContentItem, LearningService       |
| **module-billing**       | Subscription, Invoice, Payment, SchoolAccount, BillingService                   |
| **module-reporting**     | Dashboard, PerformanceIndicator, Report, StatisticsAggregator, ReportingService |
| **module-core**          | BaseEntity, NotificationService, SecurityManager, IAEngine, Utilities           |
| **module-communication** | Conversation, Message, RendezVous, Visioconference, Notification                |


## Modèle 4+1 - Vue Processus

Plateforme d’apprentissage augmentée par IA

La vue des processus décrit comment les services fonctionnent à l’exécution, indépendamment de l’organisation du code (vue de développement) ou du déploiement (infrastructure).
Elle montre les enchaînements réels des opérations, les services mobilisés et les interactions entre utilisateurs, modules et composants.

# Vue des Processus (Runtime)

### Scénario 1 — Création d’un parcours par un professeur

#### Étape 1 — Interaction utilisateur
> Le professeur ouvre le formulaire de création d’un parcours et saisit : nom du parcours, objectifs pédagogiques, élèves associés, étapes initiales (optionnel).

**Composant** : UI / Frontend  
**Action** : POST /learning-paths/create

#### Étape 2 — Appel applicatif
> Le frontend appelle le service applicatif chargé de la création du parcours.

**Composant** : LearningService  
**Rôle** : orchestrer la création du parcours

#### Étape 3 — Règles métier
> Vérification : droits du professeur, existence des élèves, validité du contenu, conformité avec l’abonnement de l’école.

**Composants** : UserService, BillingService

#### Étape 4 — Persistance
> Création des objets métier LearningPath et LearningStep.

**Composant** : LearningPathRepository

#### Étape 5 — Retour métier
> Le système renvoie au frontend l’ID du parcours, son statut et les élèves affectés.

**Composant** : UI / Frontend

#### Étape 6 — Traitements asynchrones
> Envoi de notifications aux élèves et préparation du contexte IA du parcours.

**Composants** : NotificationService, IAEngine

### Scénario 2 — Session de révision d’un élève (avec IA)

#### Étape 1 — Interaction utilisateur
> L’élève sélectionne une étape et démarre une session de révision.

**Composant** : UI / Frontend  
**Action** : clic “Commencer”

#### Étape 2 — Appel applicatif
> Le frontend demande au serveur d’initier une session de révision.

**Composant** : LearningService  
**Action** : démarrage d’une RevisionSession

#### Étape 3 — Validation métier
> Vérification : appartenance de l’élève au parcours, disponibilité de l’étape, activation du parcours, règles d’espacement des révisions.

**Composants** : UserService, LearningStepRepository

#### Étape 4 — Interaction avec l’IA
> L’élève interagit avec l’Agent IA pour poser des questions ou demander des exercices.

**Composants** : IAEngine, AgentIA, ConfigurationPrompt, SourceDocumentaire  
**Actions** : génération de réponse, filtrage pédagogique, contextualisation

#### Étape 5 — Enregistrement
> Les questions et réponses IA sont archivées pour suivi pédagogique et reporting.

**Composants** : HistoriqueInteractionRepository, ReponseIARepository

#### Étape 6 — Fin de session
> Calcul du score, du temps passé et mise à jour de la progression du parcours.

**Composants** : RevisionSessionRepository, ProgressionService

#### Étape 7 — Traitements asynchrones
> Notification au professeur + mise à jour des indicateurs de reporting.

**Composants** : NotificationService, StatisticsAggregator

### Scénario 3 — Communication professeur ⇄ élève

#### Étape 1 — Interaction utilisateur
> L’élève ouvre la messagerie, saisit un message et l’envoie à son professeur.

**Composant** : UI → WebSocket / API REST

#### Étape 2 — Appel applicatif
> Le message est transmis au service de communication.

**Composant** : CommunicationService

#### Étape 3 — Règles métier
> Vérification : droits de communication, appartenance à la même école, conformité du contenu, création ou récupération d’une conversation.

**Composants** : UserService, ConversationRepository

#### Étape 4 — Persistance
> Enregistrement du message dans la base de données.

**Composant** : MessageRepository

#### Étape 5 — Diffusion temps réel
> Le professeur reçoit le message en direct via WebSocket.

**Composant** : CommunicationGateway

#### Étape 6 — Traitements asynchrones
> Notification push si utilisateur hors-ligne + archivage pour reporting.

**Composants** : NotificationService, StatisticsAggregator

### Scénario 3 (bis) — Organisation d’une visioconférence

#### Étape 1 — Proposition de créneau
> L’élève propose un créneau de rendez-vous pour une visioconférence.

**Composant** : RendezVousService

#### Étape 2 — Acceptation
> Le professeur accepte la proposition et confirme le créneau.

**Composant** : RendezVousRepository

#### Étape 3 — Génération de la salle visio
> Le système génère un identifiant de salle virtuelle.

**Composant** : VisioconferenceService

#### Étape 4 — Notification
> Envoi d’un rappel automatique aux participants.

**Composant** : NotificationService

### Scénario 4 — Génération mensuelle d’une facture

#### Étape 1 — Déclenchement planifié
> Un job planifié détecte qu’une facture mensuelle doit être générée.

**Composant** : BillingService

#### Étape 2 — Règles métier
> Vérification : validité de l’abonnement, modules activés, nombre d’utilisateurs, calcul du montant.

**Composants** : SubscriptionRepository

#### Étape 3 — Création de la facture
> Création d'une entité Invoice avec le statut “EN_ATTENTE”.

**Composant** : InvoiceRepository

#### Étape 4 — Traitement de paiement
> Tentative de prélèvement automatique si activé par l’école.

**Composant** : PaymentGateway

#### Étape 5 — Mise à disposition
> La facture apparaît dans l’espace administrateur de l’école.

**Composant** : UI / Frontend

#### Étape 6 — Notification
> Envoi d’un email et d’une notification interne confirmant la génération de la facture.

**Composant** : NotificationService

#### Étape 7 — Reporting
> Mise à jour des indicateurs financiers du tableau de bord.

**Composant** : StatisticsAggregator

# Vue de déploiement

### Schéma de déploiement simpifié
```
┌─────────────────────┐
│  Interface Web      │
│  (React/Next.js)    │
└──────────┬──────────┘
           │
┌──────────▼──────────┐
│   API Gateway       │
│   (Golang)          │
└──────────┬──────────┘
           │
    ┌──────┴──────┬──────────┬───────────┐
    │             │          │           │
┌───▼───┐  ┌─────▼────┐ ┌──▼────┐ ┌────▼────┐
│ Auth  │  │Parcours  │ │  IA   │ │Commu-   │
│Module │  │Module    │ │Module │ │nication │
└───┬───┘  └─────┬────┘ └──┬────┘ └────┬────┘
    │            │          │           │
    └────────────┴──────────┴───────────┘
                 │
         ┌───────▼───────┐
         │   Database    │
         │  (PostgreSQL) │
         └───────────────┘
```

**Interfaces exposées :**
- API REST pour les modules
- WebSocket pour communication temps réel
- Intégration avec modèles IA externes

# Vue des scénarios (+1)

### Scénario 1 — Création d’un parcours par un professeur
 
 #### Étape 1 — Interaction utilisateur
 > Le professeur ouvre le formulaire de création d’un parcours et saisit : nom du parcours, objectifs pédagogiques, élèves associés, étapes initiales (optionnel).
 
 **Composant** : UI / Frontend  
 **Action** : POST /learning-paths/create
 
 #### Étape 2 — Appel applicatif
 > Le frontend appelle le service applicatif chargé de la création du parcours.
 
 **Composant** : LearningService  
 **Rôle** : orchestrer la création du parcours
 
 #### Étape 3 — Règles métier
 > Vérification : droits du professeur, existence des élèves, validité du contenu, conformité avec l’abonnement de l’école.
 
 **Composants** : UserService, BillingService
 
 #### Étape 4 — Persistance
 > Création des objets métier LearningPath et LearningStep.
 
 **Composant** : LearningPathRepository
 
 #### Étape 5 — Retour métier
 > Le système renvoie au frontend l’ID du parcours, son statut et les élèves affectés.
 
 **Composant** : UI / Frontend
 
 #### Étape 6 — Traitements asynchrones
 > Envoi de notifications aux élèves et préparation du contexte IA du parcours.