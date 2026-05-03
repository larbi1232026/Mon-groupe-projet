Branche Chemins (Pathfinding)
Objectif
Cette branche contient l'intelligence algorithmique permettant au personnage de naviguer de manière autonome sur le plateau. Elle assure le calcul du trajet optimal entre la position actuelle du joueur et une destination cible définie par l'utilisateur.
Responsabilités
Calculer le chemin le plus court : Utiliser des algorithmes de recherche de chemin (BFS et A*).  
Gérer l'évitement d'obstacles : Identifier les murs et les boîtes comme des cases non franchissables via le modèle.
Optimiser la performance : Utiliser une heuristique pour accélérer le calcul sur les grands niveaux.  
Traduire le chemin en actions : Transformer une suite de coordonnées en une liste de directions (UP, DOWN, LEFT, RIGHT) compréhensibles par le jeu.
Gérer les cas d'échec : Détecter les cibles inaccessibles et empêcher tout mouvement incohérent.  
Algorithmes implémentés
BFS (Breadth-First Search) : Exploration exhaustive pour garantir le chemin le plus court sur les petits plateaux.
A (A-Star) :* Recherche optimisée utilisant l'heuristique de la distance de Manhattan ($|x_1 - x_2| + |y_1 - y_2|$) pour les niveaux complexes. 
Fichiers principaux
PathFinder.java : Interface ou classe de base définissant la structure d'un chercheur de chemin.
AStarPathFinder.java : Implémentation spécifique de l'algorithme A* avec file de priorité.
BFSPathFinder.java : Implémentation du parcours en largeur classique.
Node.java : Représentation d'une étape du chemin (coordonnées, coût, lien vers le parent).
Path.java : Objet contenant la liste finale des mouvements à effectuer.
