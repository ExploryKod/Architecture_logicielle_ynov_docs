# Choix du style d'architecture de l'application 

## Contexte du projet (Rappel)

Plateforme SaaS éducative permettant aux écoles de créer des parcours d'apprentissage personnalisés avec agents IA supervisés par des professeurs.

Contraintes principales lié au projet :
- Isolation par école : application multi-tenant.
- Conformité juridique (RGPD) : nécessite bonne isolation/protection des données, accés et modification aisé.
- Temps réel & IA : montée en charge et enjeux de scalabilité conséquents.
- Domaines métiers bien identifiés et isolable
- Equipe de développement réduite (projet RNCP à faire seul pour le MVC puis peu de moyen pour mobiliser une grande équipe si développement commercial)
- Compétences en développement réduite : niveau Junior, surtout habitué au MVC et à la clean architecture, pas aux autres architectures.
- Temps limité pour développer le MVC (Délai de la formation sur quelques mois)
- Enjeux fort d'évolutivité : partir d'un MVC et migrer sans mal sur une application plus solide, partir d'un ensemble restreint de fonctionnalité mais ouvert à forte évolution.
- Enjeux fort de scalabilité/élasticité : usage d'AWS afin de se permettre une élasticité si montée subite en charge (pic) donc l'architecture doit permettre d'en profiter.
- Enjeux de sécurité et conformité lié : protection des données élèves, risques juridiques, nécessite une architecture qui isole en cas d'attaque sur un plan.



## Styles envisagé

## Analyse comparative

## Style retenu

## Justification métier

## Justification technique

## Limites et évolutions possibles