# Séance 9 - Rendre l'application dynamique

### Contexte et philosophie du projet

Notre application sert pour rappel à créer des parcours personnalisé sur-mesure afin de faciliter l'apprentissage (techniques de mémorisation, adaptation au profil etc.) des élèves de la plateforme. Elle se base pour cela sur une collaboration étroite entre élève, professeur et IA. L'IA ainsi humanisé, dans sa dimension coopérative, préfigure les usages intelligent de l'IA et non l'IA impersonnel, non-supervisé qui détruirait les métiers (ici de professeur et rend l'élève seul face à son IA-professeur). 

Notre projet modifie en profondeur le métier de professeur mais en réalité il brise le silence : cela fait déjà plus de cinquante ans voir plus que le métier de professeur n'est plus celui qui nous est imposé et nous sommes passé à côté du vrai métier de professeur. L'impact de l'IA, bien plus que n'importe quel politique publique, est peut-être à même de faire évoluer le Mammouth vers son âge réel en dehors de la période néolithique de l'éducation pour rester sur la métaphore et l'hyperbole. 

L'innovation pédagogique quand on l'évoque aujourd'hui n'est en réalité trés souvent qu'une confusion avec ajout de nouvelles technologies dans un contexte qui n'a rien changé dans la mentalité en profondeur. Cette plateforme pourrait n'être que ça.

Ce n'est donc pas une simple plateforme d'IA comme il y en a plein d'autres, c'est un projet pédagogique complexe qui implique l'humain et donc toute les sciences en lien. Une équipe de développeur et son PO n'est pas à même de mener un tel projet sans l'expérience d'autres professions, des écoles et des professeurs.

C'est pourquoi, rendre cette application dynamique implique : 
- De ne pas se contenter de modifier le métier en surface (typologies etc...) mais de rendre compte d'une philosophie pédagogique nouvelle. 

### Problématisation et outils

Nous prenons pour exemple l'assignation d'un parcours d'apprentissage (LearningPath). Nous avons deux problèmes A et B.
- (A) Plusieurs niveau pour un même parcours pose problème : on pourrait mettre dans chaque parcours sa méthode de gestion des niveaux avec des if/else complexe selon le parcours. Il y a une multiplicité de parcours variables : ils varient car ils sont créé sur-mesure selon un ensemble de profil d'élèves. Ils varient aussi en terme de niveaux. Comment assigner un niveau cohérent - créer par ailleurs par l'école 
Exemple avec le TOEIC : A1, B2, C1 ont une signification unifié, lisible sur le marché de l'emploi. Nous voulons créer la même lisibilité dans l'application. Or nos parcours eux sont différents. On serait tenté de mettre dans chaque parcours son niveau avec ses méthodes spécifiques. On se retrouve avec des centaines de if/else répartie dans chaque parcours quand l'application prend de l'ampleur et chacun créer ses catégories de parcours.
- (B) Il faut en plus rapporté au professeur des informations diverses au regard de chaque parcours qui sont des notifications pour alerter sur le suivi du parcours. On ne peux pas non plus mettre cela partout et se répéter en plus car nous n'avons pas de pattern capable d'unifier ces notification dans une logique Open/Closed principle qui garantie l'intégrité de la règle métier (sa stabilité malgré ce qui varit).

Deux patterns permettent de répondre :
- Le pattern strategy qui va réduire la complexité inutile de ce qui est variable dans le métier sans connaître les détails d'implémentation. Il modifie une régle existante de base sans la remplacer mais le résultat est différent selon les différentes stratégies car ce résultat dépend du facteur variable. 
- Le pattern décorator qui permet d'enrichir le métier dans la simplicité : contrairement à la strategy, il ne modifie pas la règle métier, il ajoute un plus.

Dans les deux cas ils permettent de rendre l'application testable, maintenable, robuste, découplé.... et consolide le respect de tous les piliers du développement logiciel sans dette technique.

### Résolution de la problématique : pseudo-code sur notre exemple (strategy + decorator)

1. Pattern strategy : résoudre le problème A

2. Pattern decorator : résoudre le problème B