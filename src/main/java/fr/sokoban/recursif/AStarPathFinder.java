package fr.sokoban.recursif;

import java.util.*;

// Trouve le plus court chemin avec l'algorithme A*.
// Plus efficace que BFS sur les grands plateaux
// car il utilise une heuristique pour explorer en priorité
// les cases proches de l'arrivée.

public class AStarPathFinder {

    // Noeud interne pour la file de priorité
    private static class Noeud implements Comparable<Noeud> {
        Position pos;
        int cout;       // nombre de pas depuis le départ
        int heuristique; // estimation de la distance restante

        Noeud(Position pos, int cout, int heuristique) {
            this.pos        = pos;
            this.cout       = cout;
            this.heuristique = heuristique;
        }

        int coutTotal() { return cout + heuristique; }

        @Override
        public int compareTo(Noeud autre) {
            return Integer.compare(this.coutTotal(), autre.coutTotal());
        }
    }

    // Retourne la liste de directions pour aller de "depart" à "arrivee".
    // Retourne null si aucun chemin n'existe.
    public static List<Direction> trouverChemin(World monde, Position depart, Position arrivee) {
        if (depart.equals(arrivee)) return new ArrayList<>();

        PriorityQueue<Noeud> file = new PriorityQueue<>();
        Map<Position, Position>  parent    = new HashMap<>();
        Map<Position, Direction> direction = new HashMap<>();
        Map<Position, Integer>   couts     = new HashMap<>();

        file.add(new Noeud(depart, 0, manhattan(depart, arrivee)));
        parent.put(depart, null);
        couts.put(depart, 0);

        while (!file.isEmpty()) {
            Noeud courant = file.poll();
            Position pos  = courant.pos;

            if (pos.equals(arrivee)) {
                return reconstruireChemin(depart, arrivee, parent, direction);
            }

            for (Direction dir : Direction.values()) {
                Position voisine = pos.move(dir);

                if (!monde.isInside(voisine))  continue;
                if (monde.isWall(voisine))      continue;
                if (monde.hasBoxAt(voisine))    continue;

                int nouveauCout = couts.get(pos) + 1;

                if (!couts.containsKey(voisine) || nouveauCout < couts.get(voisine)) {
                    couts.put(voisine, nouveauCout);
                    parent.put(voisine, pos);
                    direction.put(voisine, dir);
                    file.add(new Noeud(voisine, nouveauCout, manhattan(voisine, arrivee)));
                }
            }
        }

        return null;
    }

    // Distance de Manhattan : nombre de cases en ligne droite (sans diagonale)
    private static int manhattan(Position a, Position b) {
        return Math.abs(a.row() - b.row()) + Math.abs(a.col() - b.col());
    }

    private static List<Direction> reconstruireChemin(
            Position depart, Position arrivee,
            Map<Position, Position> parent,
            Map<Position, Direction> direction) {

        LinkedList<Direction> chemin = new LinkedList<>();
        Position courante = arrivee;

        while (!courante.equals(depart)) {
            chemin.addFirst(direction.get(courante));
            courante = parent.get(courante);
        }

        return chemin;
    }
}
