package fr.sokoban.classique;

import java.util.List;

import java.util.List;

public class SolverNode {
    public final SolverState state;
    public final List<Direction> path;
    public final int cost;
    public final int priority;

    public SolverNode(SolverState state, List<Direction> path, int cost, int priority) {
        this.state = state;
        this.path = path;
        this.cost = cost;
        this.priority = priority;
    }
}
