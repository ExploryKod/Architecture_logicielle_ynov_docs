# M2 Expert développeur web - Ynov

Rappel du contexte du projet : 
L’entreprise fictive serait un fournisseur d’espaces au sein de cette plateforme d’apprentissage qui
agit comme un SAAS modulable. A l’image de Moodle Cloud qui fournit des espaces de “Learning
Management System” (LMS) afin que les écoles personnalisent les espaces d’échanges avec les
étudiants, nous fournissons des espaces qui permettent de créer des parcours d’apprentissage sur-
mesure avec des outils poussés de mémorisation personnalisable par élève, par matière et par
professeur.

**La version de synthèse :** [Synthèse des vues](#synth%C3%A8se-des-vues)

**Les 4 parties du détail par vue (liens interne au doc) :** 

- [Vue logique](#mod%C3%A8le-41---vue-logique)
- [Vue développement](#mod%C3%A8le-41---vue-d%C3%A9veloppement)
- [Vue processus](#mod%C3%A8le-41---vue-processus)
- [Vue déploiement](#mod%C3%A8le-41-vue-de-d%C3%A9ploiement)

# Synthèse des vues

Avant d'entrer dans le détail et les schémas, voici une version de synthèse débarrassée du jargon trop technique. 

## Vue logique

L'application s'organise en blocs qui regroupent des règles métiers cohérentes entre-elles : 
1. la gestion utilisateur,
2. les parcours d'apprentissages,
3. le travail sur les données issue des IA,
4. les outils de communication et notification (chat, agenda, emails, visio-conférence....),
5. le reporting des progrés élèves et de l'app en général (archives, documents, logs, statistiques analytiques matomo etc.),
6. la facuration et paiement pour profiter de la plateforme. 

Les blocs ont des relations entre-eux : ces relations sont cruciales pour comprendre comment notre application gère les liens entre les règles métiers si elles sont transverses.

**De ces blocs nous avons donc exposé des parcours utilisateurs cohérents:**
- **Bloc 1 / 2** : Les utilisateurs gèrent leurs parcours d'apprentissages et en retour les parcours retourne un feedback (professeurs et élèves génèrent des parcours ensemble et reçoivent en retour des vues de parcours).
- **Bloc 2 / 3** : Les IA produisent des données structurées à partir des espaces d'analyse pour l'IA créé dans les parcours (eux-même alimentés par les utilisateurs) et les parcours se servent des retour de l'IA pour s'auto-enrichir (Et communique cela aux utilisateurs qui peuvent aussi enrichir manuellement les parcours).
- **Bloc 1 / 4** : les utilisateurs selon leur rôle (professeurs, support technique, écoles ou élèves) utilise les outils de communication (chats, emails, visio-conférence, agenda, diagramme de gantt...) afin de jouer leur rôle via une présence plus humaine et un suivi. En retour ces outils leur renvoit un statut, donne des espaces approprié selon les profils, authentifie et protège via un middleware et donne les bonnes vues selon les demandes.
- **Bloc 1 / 5** : Le reporting sert aux utilisateurs pour, selon leur rôle, avoir des informations qui permettent de mieux jouer ce rôle et avoir une vision moyen et long terme. Par exemple des indications de performance sur les parcours et la progression des elèves aide les professeurs mais le bloc a une acception trés large et il donne aussi des logs technique au support technique.
- **Bloc 1 / 6** : Les utilisateur selon leur rôle interagissent avec les modules de paiement et abonnements, reçoivent des notifications ou sont redirigés vers les bon espaces pour régler une facture. Cela implique les catégories d'utilisateurs selon leur rôle (les écoles sont les seules à souscrire un plan tarifaire, les professeurs sont enrôlés selon les plans tarifaires choisis et sont notifiés de leur statut et les accés possible selon ce plan tarifaire, etc...)

De cela nous tiront des relations entre les tables d'une base de donnée via un diagramme MCD (ou entre des collections si NoSQL). 
=> Voir ces relations dans le détail.

## Vue de développement

Cette vue permet de mieux isolé ce que deviendra concrètement le code de l'application. Nous retrouvons le lien avec la vue précèdent et les règles métiers mais ici le tableau suivant donne des class qui, dans le cadre d'une programmation orienté objet, permettent d'articuler via les design pattern issues de la POO et les pattern architecturaux plus globaux les règles métiers entre-elles. Nous organisons le code via des modules maven bien qui ont de faible liens de dépendances entre-eux afin de créer un "monolithe modulaire" donc des modules qui ne doivent pas être tous réécrit si nous ajoutons une fonctionnalité ou devons résoudre un bug. 

Voici ce qui permet à l'app de devenir scalable, évolutive et maintenable : 

| **Module**               | **Classes clés**                                                                |
| ------------------------ | ------------------------------------------------------------------------------- |
| **module-users**         | User, StudentProfile, Role, Permission, UserService                             |
| **module-learning**      | LearningPath, LearningStep, RevisionSession, ContentItem, LearningService       |
| **module-billing**       | Subscription, Invoice, Payment, SchoolAccount, BillingService                   |
| **module-reporting**     | Dashboard, PerformanceIndicator, Report, StatisticsAggregator, ReportingService |
| **module-core**          | BaseEntity, NotificationService, SecurityManager, IAEngine, Utilities           |
| **module-communication** | Conversation, Message, RendezVous, Visioconference, Notification                |

## Vue des processus

Comment fonctionne réellement l'application à l'exécution ? Cette vue nous montre via différent scénarios, les processus orchestrés lorsque l'application marche en ligne. 
La plateforme repose sur une architecture orientée services, combinant interactions utilisateur, règles métier, persistance des données et traitements asynchrones.

> Nous avons pris un exemple dont le détail est dans la vue de détail :<br>
**Création d’un parcours par un professeur et usage du parcours par l'èlève.**

Ici nous exposons juste les étapes (synthèse) pour le scénario mentionné ci-dessus.

1. Création et gestion d’un parcours pédagogique (Professeur)
- Le professeur initie la création d’un parcours via l’interface utilisateur.
- Le frontend transmet la demande au LearningService, qui orchestre le processus.
- Les règles métier sont vérifiées (droits, validité des élèves, conformité à l’abonnement) en s’appuyant sur les services utilisateurs et de facturation.
- Une fois validé, le parcours et ses étapes sont persistés en base.
- Le système retourne au frontend les informations clés (ID, statut, élèves associés).
- Des traitements asynchrones complètent le flux : notifications aux élèves et préparation du contexte IA du parcours.
**Objectif**: garantir un parcours valide, traçable et prêt à être exploité pédagogiquement.

2. Session de révision d’un élève avec IA
- L’élève démarre une session de révision depuis une étape du parcours.
- Le LearningService initialise une session après validation métier (droits, état du parcours, règles de révision).
- L’interaction principale se fait avec l’Agent IA, qui génère des réponses contextualisées et pédagogiquement filtrées à partir des contenus du parcours.
- Toutes les interactions sont archivées pour le suivi pédagogique.
- En fin de session, les résultats (score, temps, progression) sont calculés et enregistrés.
- Des notifications et mises à jour statistiques sont déclenchées de manière asynchrone.
**Objectif** : offrir un accompagnement personnalisé tout en assurant un suivi mesurable.

3. Communication professeur ⇄ élève (messagerie et visio)
- La messagerie permet des échanges temps réel via API/WebSocket.
- Le CommunicationService vérifie les droits, gère les conversations et persiste les messages.
- La diffusion se fait en direct si le destinataire est connecté, sinon via notification asynchrone. 
**Objectif** : faciliter des échanges sécurisés, synchrones ou asynchrones, dans un cadre scolaire.

4. Facturation et administration selon le parcours d'apprentissage (École / Utilisateurs)
- Un job planifié déclenche mensuellement la génération des factures en fonction du plan et de la consommation lié au parcours généré.
- Le BillingService applique ou vérifie les règles d’abonnement (modules, utilisateurs, montants).
**Objectif** : automatiser une facturation fiable, transparente et traçable.

## Vue de déploiement

La **vue de déploiement** représente l'**infrastructure physique** et la **distribution des composants logiciels** sur les différents serveurs. Nous allons utiliser des schémas en markdown pour la décrire avec dans un premier temps une vision de l'application elle-même puis dans un second temps l'infrastructure complète. 

Nous avons décidé de créer l'application en deux étapes : 
- Une première avec l'usage d'un VPS pour un MVC de l'application pour tester le marché et les retours utilisateurs
- Une seconde avec un déploiement qui doit tenir la charge via un service AWS de cloud.

Pour avoir plus de détail sur cette vue avec les schémas complet : se référer à la vue dans le détail.

Nous récapitulons ici l'architecture choisi. Que ce soit sur AWS ou sur le VPS, nous conteneurisons nos environnements via Docker. 
Nous parlons donc sous un format de conteneurs isolés et portable. AWS permet une élasticité que le VPS ne permet pas mais s'articule bien avec Docker aussi. 

Nous avons un monolithe modulaire à hébergé qui n'est pas stricto-censu du micro-service car une base de donnée peut être partagé entre plusieurs services. Pour autant, les modules appartiennent à des conteneurs bien séparés et autonome sur certains plans ce qui permet une grande flexibilité pour réorganiser les liens entre ces conteneurs. AWS pourra permettre en outre d'offrir via des load balancer et aussi des outils comme Kubernetes la capacité de créer des outils de backup ou des système de copies (avec un principal et ses répliques (replica) prêt à le remplacer). AWS permet même d'organiser cela sur plusieurs région et limiter les problème si une panne n'affecte qu'une région du monde.

Nos conteneurs : 
- Le frontend avec un backend léger (via NextJs)
- Une API en Golang chargé de gérer les interactions (fine tunning, RAG...) avec l'IA et ses modèles.
- Une autre API en Golang mais chargé de créer des websockets pour les parcours d'apprentissage (si nécessaire pour des jeux etc.) et les outils de communication (chat etc.).
- Une ou plusieurs bases de données (chacune avec son conteneur) PostgreSQL pour persister les données issue de l'app frontend et des api (mais on devrait passer via l'app)
- Un conteneur dédié à Redis pour la performance et mise en cache des flux de données
- Un conteneur Ollama : un outil permettant d'héberger ses propres modèles d'IA sur son propre serveur en interaction avec le conteneur de l'API Golang. (1)

(1) Nous devons voir si il n'est pas mieux de tout regrouper dans un seul conteneur avec l'api Golang selon l'organisation la meilleure mais aussi si Golang sait aussi directement géré du Ollama (usage plutôt en Python habituellement or Golang a des connecteurs pour les outils de RAG mais pas aussi complet que Python).

Regardez la partie sur les détails pour en savoir plus avec les noms exact des services AWS mobilisé (EC2 etc.) ou les services du VPS pour le MVC. 

---

# Détails par vue (Modèle 4+1)

## Modèle 4+1 - Vue Logique 

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

La vue des processus décrit comment les services fonctionnent à l’exécution, indépendamment de l’organisation du code (vue de développement) ou du déploiement (infrastructure).

### Scénario (Exemple) — Création d’un parcours par un professeur

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

## Modèle 4+1: Vue de déploiement

La **vue de déploiement** représente l'**infrastructure physique** et la **distribution des composants logiciels** sur les différents serveurs. Nous allons utiliser des schémas en markdown pour la décrire avec dans un premier temps une vision de l'application elle-même puis dans un second temps l'infrastructure complète. 

L'infrastructure complète se distingue via deux versions : 
- Une version du MVC qui sera hébergé sur un ou plusieurs VPS hostinger
- Une version de production finale qui sera héberger sur AWS

### Schéma de l'application (vue applicative seulement)

Voici tout d'abord une vue de l'application sans l'infrastructure de déploiement complet mais simplement une première vue pour mieux se repérer. 
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

### Schéma de déploiement (détails avec deux versions: le MVC et la vue de production finale)

##### Architecture de déploiement : MVP sur un VPS Hostinger

```
┌──────────────────────────────────────────────┐
│         VPS Hostinger (Europe - RGPD)        │
├──────────────────────────────────────────────┤
│                                              │
│  ┌────────────────────────────────────┐     │
│  │   Docker Compose Stack             │     │
│  ├────────────────────────────────────┤     │
│  │                                    │     │
│  │  ┌──────────────────────┐         │     │
│  │  │  Container: Frontend │         │     │
│  │  │  (Next.js - SSR)     │         │     │
│  │  │  Port: 3000          │         │     │
│  │  └──────────────────────┘         │     │
│  │                                    │     │
│  │  ┌──────────────────────┐         │     │
│  │  │  Container: API      │         │     │
│  │  │  (Golang)            │         │     │
│  │  │  Port: 8080          │         │     │
│  │  └──────────────────────┘         │     │
│  │                                    │     │
│  │  ┌──────────────────────┐         │     │
│  │  │  Container: WebSocket│         │     │
│  │  │  (Chat temps réel)   │         │     │
│  │  │  Port: 8081          │         │     │
│  │  └──────────────────────┘         │     │
│  │                                    │     │
│  │  ┌──────────────────────┐         │     │
│  │  │  Container: PostgreSQL│        │     │
│  │  │  Port: 5432          │         │     │
│  │  │  + Volume persistant │         │     │
│  │  └──────────────────────┘         │     │
│  │                                    │     │
│  │  ┌──────────────────────┐         │     │
│  │  │  Container: Redis    │         │     │
│  │  │  (Cache + Sessions)  │         │     │
│  │  │  Port: 6379          │         │     │
│  │  └──────────────────────┘         │     │
│  │                                    │     │
│  │  ┌──────────────────────┐         │     │
│  │  │  Container: Ollama   │         │     │
│  │  │  (LLM local - dev)   │         │     │
│  │  │  Port: 11434         │         │     │
│  │  └──────────────────────┘         │     │
│  │                                    │     │
│  └────────────────────────────────────┘     │
│                                              │
│  ┌────────────────────────────────────┐     │
│  │   Nginx Reverse Proxy              │     │
│  │   - SSL/TLS (Let's Encrypt)        │     │
│  │   - Port 80 → 443                  │     │
│  └────────────────────────────────────┘     │
│                                              │
└──────────────────────────────────────────────┘
```

**Configuration de ce VPS :**
- **CPU :** 4 vCPUs minimum
- **RAM :** 16 GB (nécessaire pour Ollama + conteneurs)
- **Stockage :** 200 GB SSD
- **Bande passante :** Illimitée
- **Localisation :** Amsterdam ou Francfort (RGPD)
  
##### Architecture de déploiement : environnement de production final avec AWS Multi-AZ.

 Nous créons l'app en production sur une architecture Haute Disponibilité avec l'aide des options offertes par AWS



```
┌──────────────────────────────────────────────────────────────┐
│                    AWS Region: eu-west-1                     │
│                      (Irlande - RGPD)                        │
└──────────────────────────────────────────────────────────────┘
                             │
                ┌────────────┴────────────┐
                │                         │
         ┌──────▼──────┐          ┌──────▼──────┐
         │Availability │          │Availability │
         │   Zone A    │          │   Zone B    │
         └─────────────┘          └─────────────┘


┌─────────────────────────────────────────────────────────────┐
│                   COUCHE RÉSEAU / SÉCURITÉ                  │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌───────────────────────────────────────────────┐         │
│  │  AWS Application Load Balancer (ALB)          │         │
│  │  - Health checks                              │         │
│  │  - SSL/TLS Termination                        │         │
│  │  - Distribution de charge                     │         │
│  └───────────────────────────────────────────────┘         │
│                                                             │
│  ┌───────────────────────────────────────────────┐         │
│  │  AWS WAF (Web Application Firewall)           │         │
│  │  - Protection DDoS                            │         │
│  │  - Filtrage requêtes malveillantes            │         │
│  └───────────────────────────────────────────────┘         │
│                                                             │
└─────────────────────────────────────────────────────────────┘
                             │
                ┌────────────┴────────────┐
                │                         │
                ▼                         ▼

┌──────────────────────┐      ┌──────────────────────┐
│   PUBLIC SUBNET A    │      │   PUBLIC SUBNET B    │
│   (DMZ)              │      │   (DMZ)              │
├──────────────────────┤      ├──────────────────────┤
│                      │      │                      │
│  EC2: NAT Gateway    │      │  EC2: NAT Gateway    │
│                      │      │                      │
└──────────────────────┘      └──────────────────────┘
         │                             │
         └──────────┬──────────────────┘
                    │
     ┌──────────────┴──────────────┐
     │                             │
     ▼                             ▼

┌──────────────────────┐      ┌──────────────────────┐
│  PRIVATE SUBNET A    │      │  PRIVATE SUBNET B    │
│  (Application Tier)  │      │  (Application Tier)  │
├──────────────────────┤      ├──────────────────────┤
│                      │      │                      │
│ ┌────────────────┐  │      │ ┌────────────────┐  │
│ │ Auto Scaling   │  │      │ │ Auto Scaling   │  │
│ │ Group          │  │      │ │ Group          │  │
│ ├────────────────┤  │      │ ├────────────────┤  │
│ │                │  │      │ │                │  │
│ │ EC2: Frontend  │  │      │ │ EC2: Frontend  │  │
│ │ (Next.js)      │  │      │ │ (Next.js)      │  │
│ │ Min: 2         │  │      │ │ Min: 2         │  │
│ │ Max: 10        │  │      │ │ Max: 10        │  │
│ │                │  │      │ │                │  │
│ └────────────────┘  │      │ └────────────────┘  │
│                      │      │                      │
│ ┌────────────────┐  │      │ ┌────────────────┐  │
│ │ Auto Scaling   │  │      │ │ Auto Scaling   │  │
│ │ Group          │  │      │ │ Group          │  │
│ ├────────────────┤  │      │ ├────────────────┤  │
│ │                │  │      │ │                │  │
│ │ EC2: API       │  │      │ │ EC2: API       │  │
│ │ (Golang)       │  │      │ │ (Golang)       │  │
│ │ Min: 3         │  │      │ │ Min: 3         │  │
│ │ Max: 15        │  │      │ │ Max: 15        │  │
│ │                │  │      │ │                │  │
│ └────────────────┘  │      │ └────────────────┘  │
│                      │      │                      │
│ ┌────────────────┐  │      │ ┌────────────────┐  │
│ │ EC2: WebSocket │  │      │ │ EC2: WebSocket │  │
│ │ (Chat/Visio)   │  │      │ │ (Chat/Visio)   │  │
│ │ Min: 2         │  │      │ │ Min: 2         │  │
│ └────────────────┘  │      │ └────────────────┘  │
│                      │      │                      │
└──────────────────────┘      └──────────────────────┘
         │                             │
         └──────────┬──────────────────┘
                    │
     ┌──────────────┴──────────────┐
     │                             │
     ▼                             ▼

┌──────────────────────┐      ┌──────────────────────┐
│  PRIVATE SUBNET C    │      │  PRIVATE SUBNET D    │
│  (Data Tier)         │      │  (Data Tier)         │
├──────────────────────┤      ├──────────────────────┤
│                      │      │                      │
│ ┌────────────────┐  │      │ ┌────────────────┐  │
│ │ RDS PostgreSQL │  │      │ │ RDS PostgreSQL │  │
│ │ (Master)       │◄─┼──────┼─┤ (Read Replica) │  │
│ │                │  │      │ │                │  │
│ │ Multi-AZ       │  │      │ │                │  │
│ │ Automated      │  │      │ │                │  │
│ │ Backups        │  │      │ │                │  │
│ └────────────────┘  │      │ └────────────────┘  │
│                      │      │                      │
│ ┌────────────────┐  │      │ ┌────────────────┐  │
│ │ ElastiCache    │  │      │ │ ElastiCache    │  │
│ │ Redis Cluster  │◄─┼──────┼─┤ Redis Cluster  │  │
│ │ (Primary)      │  │      │ │ (Replica)      │  │
│ └────────────────┘  │      │ └────────────────┘  │
│                      │      │                      │
└──────────────────────┘      └──────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                   SERVICES EXTERNES AWS                     │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────────────────────────────────┐           │
│  │  Amazon S3 (Stockage)                       │           │
│  │  - Fichiers uploadés (PDFs, images)         │           │
│  │  - Backups base de données                  │           │
│  │  - Logs applicatifs                         │           │
│  │  - Versioning activé                        │           │
│  └─────────────────────────────────────────────┘           │
│                                                             │
│  ┌─────────────────────────────────────────────┐           │
│  │  Amazon SES (Email)                         │           │
│  │  - Envoi notifications                      │           │
│  │  - Invitations utilisateurs                 │           │
│  └─────────────────────────────────────────────┘           │
│                                                             │
│  ┌─────────────────────────────────────────────┐           │
│  │  Amazon CloudWatch                          │           │
│  │  - Monitoring CPU/RAM/Network               │           │
│  │  - Logs centralisés                         │           │
│  │  - Alarmes automatiques                     │           │
│  └─────────────────────────────────────────────┘           │
│                                                             │
│  ┌─────────────────────────────────────────────┐           │
│  │  AWS Secrets Manager                        │           │
│  │  - Clés API OpenAI/Anthropic                │           │
│  │  - Credentials DB                           │           │
│  │  - Tokens JWT                               │           │
│  └─────────────────────────────────────────────┘           │
│                                                             │
│  ┌─────────────────────────────────────────────┐           │
│  │  Amazon SageMaker (ou EC2 GPU)              │           │
│  │  - Hébergement modèles IA custom            │           │
│  │  - Inférence Langchain + RAG                │           │
│  │  - Instance GPU (g4dn.xlarge)               │           │
│  └─────────────────────────────────────────────┘           │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

##### Architecture de déploiement : couche de sécurité.
Aussi, nous devons prendre en compte la sécurité, voici les zones de Sécurité par niveau :

```
┌──────────────────────────────────────────┐
│  ZONE PUBLIQUE (Internet)                │
│  - CloudFlare WAF                        │
│  - AWS ALB + AWS WAF                     │
└──────────────┬───────────────────────────┘
               │ Firewall
               ▼
┌──────────────────────────────────────────┐
│  ZONE DMZ (Public Subnets)               │
│  - NAT Gateways seulement                │
│  - Pas d'applications exposées           │
└──────────────┬───────────────────────────┘
               │ Security Groups
               ▼
┌──────────────────────────────────────────┐
│  ZONE APPLICATION (Private Subnets)      │
│  - Frontend instances                    │
│  - API instances                         │
│  - WebSocket instances                   │
│  Règles :                                │
│  • Accepte traffic depuis ALB uniquement │
│  • Peut sortir vers internet via NAT     │
└──────────────┬───────────────────────────┘
               │ Security Groups stricts
               ▼
┌──────────────────────────────────────────┐
│  ZONE DATA (Private Subnets isolés)      │
│  - RDS PostgreSQL                        │
│  - ElastiCache Redis                     │
│  Règles :                                │
│  • Accepte traffic APP tier uniquement   │
│  • AUCUN accès internet                  │
│  • Chiffrement en transit (TLS)          │
│  • Chiffrement at rest (AES-256)         │
└──────────────────────────────────────────┘
```

##### Architecture de déploiement : vue globale.

Voici enfin un schéma simplifié qui récapitule notre architecture de production (donc pas celle du MVC). 

```
                    INTERNET
                       │
                       ▼
              ┌─────────────────┐
              │   CloudFlare    │
              │   + Route 53    │
              └────────┬────────┘
                       │
                       ▼
              ┌─────────────────┐
              │   AWS ALB       │
              │   + WAF         │
              └────────┬────────┘
                       │
         ┌─────────────┼─────────────┐
         │             │             │
         ▼             ▼             ▼
    ┌────────┐   ┌────────┐   ┌──────────┐
    │Frontend│   │  API   │   │WebSocket │
    │Next.js │   │ Golang │   │Chat/Visio│
    │        │   │        │   │          │
    │Auto    │   │Auto    │   │Dedicated │
    │Scale   │   │Scale   │   │Instances │
    │2-10    │   │3-15    │   │2-4       │
    └───┬────┘   └───┬────┘   └────┬─────┘
        │            │             │
        └────────────┼─────────────┘
                     │
         ┌───────────┼───────────┐
         │           │           │
         ▼           ▼           ▼
    ┌────────┐  ┌───────┐  ┌─────────┐
    │   RDS  │  │ Redis │  │    S3   │
    │Postgres│  │Cache  │  │ Fichiers│
    │Multi-AZ│  │Cluster│  │         │
    └────────┘  └───────┘  └─────────┘

              ┌──────────────┐
              │ EC2 GPU      │
              │ SageMaker    │
              │ (IA/RAG)     │
              └──────────────┘
```

Cette architecture hybride VPS (pour le MVC) + AWS (production finale) a les atout suivants :

**Scalabilité** : Auto-scaling horizontal sur tous les tiers
**Haute disponibilité** : Multi-AZ, load balancing, failover auto
**Sécurité** : Isolation réseau, chiffrement, RGPD-compliant
**Performance** : CDN, caching, instances optimisées
**Maintenabilité** : Infrastructure as Code, monitoring complet
**Coût maîtrisé** : Optimisations possibles (-30-40%)

La séparation claire entre environnement de développement (VPS simple) et production (AWS managé) permet d'itérer rapidement tout en garantissant la robustesse en production.

## Modèle 4+1 - Vue des scénarios (+1)

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
