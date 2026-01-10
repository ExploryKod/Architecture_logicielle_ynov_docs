# Facade (séance 8)

## Contexte

Plateforme SaaS éducative permettant aux écoles de créer des parcours d'apprentissage personnalisés avec agents IA supervisés par des professeurs.

## Fonctionnalités majeures

- L'élève peut se créer un profil sur-mesure à partir d'un questionnaire et autres ressources
- Le professeur personnalise un parcours d'apprentissage pour un élève à partir de son profil
- Création d'un parcours par le professeur et l'élève via un échange suivi d'une mobilisation des ressources IA
- Validation d'un parcours par un professeur
- Lister les parcours validés disponibles pour assignation aux élèves

## LearningPathFacade

**Rôle** : Exposer les règles métier des parcours d'apprentissage, organiser le flux de création, validation et assignation.

**Services utilisés** :
- LearningService (création, validation, assignation de parcours)
- UserService (vérification des droits, récupération des élèves)
- NotificationService (envoi de notifications aux élèves et professeurs)
- BillingService (vérification de l'abonnement de l'école)

**Méthodes métier** :

```
createLearningPath(teacherId, title, description, studentIds)
  Crée un parcours pédagogique pour des élèves spécifiques
  Vérifie les droits du professeur
  Vérifie l'abonnement de l'école
  Notifie les élèves de la création

validateLearningPath(teacherId, pathId)
  Valide un parcours pour le rendre disponible aux élèves
  Vérifie que le professeur est le créateur
  Change le statut du parcours en VALIDATED
  Notifie les élèves concernés

assignPathToStudent(teacherId, pathId, studentId)
  Assigne un parcours validé à un élève
  Vérifie que le parcours est validé
  Vérifie que l'élève appartient à l'école du professeur
  Crée l'assignation et notifie l'élève

listValidatedPathsForSchool(schoolId)
  Liste tous les parcours validés disponibles pour une école
  Filtre uniquement les parcours VALIDATED
  Retourne les parcours prêts pour assignation
```

**Schéma du flux de validation et assignation** :

```
Professeur
    ↓
createLearningPath()
    ↓
LearningPath (DRAFT)
    ↓
validateLearningPath()
    ↓
LearningPath (VALIDATED)
    ↓
assignPathToStudent()
    ↓
PathAssignment créé
    ↓
NotificationService
    ↓
Élève notifié
```

## StudentProfileFacade

**Rôle** : Exposer les règles métier de gestion des profils élèves, simplifier la création et la personnalisation.

**Services utilisés** :
- UserService (création et gestion des utilisateurs)
- StudentProfileService (création et mise à jour du profil)
- QuestionnaireService (traitement des réponses au questionnaire)

**Méthodes métier** :

```
createStudentProfile(studentId, questionnaireAnswers, preferences)
  Crée un profil personnalisé pour un élève
  Traite les réponses au questionnaire
  Enregistre les préférences d'apprentissage
  Génère les recommandations initiales

updateStudentProfile(studentId, preferences)
  Met à jour les préférences d'apprentissage d'un élève
  Recalcule les recommandations si nécessaire

getStudentProfileForPersonalization(studentId)
  Récupère le profil complet d'un élève
  Utilisé par le professeur pour personnaliser un parcours
  Retourne les préférences, forces, faiblesses identifiées
```

**Schéma du flux de création de profil** :

```
Élève
    ↓
Répond au questionnaire
    ↓
createStudentProfile()
    ↓
QuestionnaireService traite les réponses
    ↓
StudentProfile créé avec préférences
    ↓
Recommandations générées
```

## PathCreationFacade

**Rôle** : Exposer les règles métier de création collaborative de parcours avec assistance IA, organiser l'échange professeur-élève-IA.

**Services utilisés** :
- LearningService (création du parcours)
- IAEngine (génération de contenu pédagogique)
- CommunicationService (échange professeur-élève)
- UserService (vérification des droits)

**Méthodes métier** :

```
initiateCollaborativePathCreation(teacherId, studentId, initialObjectives)
  Démarre une création collaborative de parcours
  Crée une conversation dédiée professeur-élève
  Initialise le contexte IA avec les objectifs
  Retourne l'identifiant de la session de création

addContributionToPath(sessionId, contributorId, contribution, contributionType)
  Ajoute une contribution (professeur ou élève) au parcours en création
  Enregistre la contribution dans la conversation
  Met à jour le contexte IA
  Notifie l'autre contributeur

generatePathContentWithAI(sessionId, request)
  Demande à l'IA de générer du contenu pour le parcours
  Utilise le contexte de la conversation
  Retourne des suggestions de contenu pédagogique
  Filtre les suggestions selon les règles pédagogiques

finalizeCollaborativePath(sessionId, teacherId)
  Finalise le parcours créé collaborativement
  Crée le LearningPath avec le statut DRAFT
  Archive la conversation
  Notifie l'élève que le parcours est prêt pour validation
```

**Schéma du flux de création collaborative** :

```
Professeur + Élève
    ↓
initiateCollaborativePathCreation()
    ↓
Session de création + Conversation créée
    ↓
Échange de contributions
    ↓
addContributionToPath() (professeur)
    ↓
addContributionToPath() (élève)
    ↓
generatePathContentWithAI()
    ↓
IA génère des suggestions
    ↓
finalizeCollaborativePath()
    ↓
LearningPath (DRAFT) créé
```

## Bénéfices des Facades

**Simplification** : Un seul point d'entrée pour des opérations métier complexes qui nécessitent plusieurs services.

**Langage métier** : Les méthodes utilisent un vocabulaire compréhensible par les acteurs métier (professeur, élève, parcours, profil).

**Organisation du flux** : Chaque Facade orchestre les appels aux services dans le bon ordre, garantissant le respect des règles métier.

**Découplage** : Le frontend n'a pas besoin de connaître tous les services techniques. Il appelle simplement la Facade avec des paramètres métier.
